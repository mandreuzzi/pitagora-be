package it.apeiron.pitagora.core.quartz;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.ERROR_HTTPSOURCE_SCHEDULING;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset.DatasetGenerator;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@RequiredArgsConstructor
@Service
public class JobService {
    public static final String SOURCE_ID = "SOURCE_ID";
    public static final String MAPPER_ID = "MAPPER_ID";
    public static final String DATASET_ID = "DATASET_ID";

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
        init();
    }

    private final Scheduler scheduler;
    private final PitagoraJobRepository jobRepository;

    @Transactional
    public void init() {
        try {
            scheduler.start();

            List<PitagoraJob> jobs = jobRepository.findAll();

            for (PitagoraJob job : jobs) {

                try {

                    JobDetail jobDetail = scheduleJob(job.getDatasetId(), job.getSourceId(), job.getMapperId());
                    job.setJobKey(jobDetail.getKey().getName());
                    jobRepository.save(job);

                } catch (PitagoraException e) {
                    log.info("Cannot restart Job with key " + job.getJobKey());
                }

            }

        } catch (SchedulerException e) {
            log.error("Error starting Quartz Scheduler");
            throw PitagoraException.internalServerError();
        }
    }

    @Transactional
    public void startJob(ObjectId datasetId, ObjectId sourceId, ObjectId mapperId) throws SchedulerException {
        JobDetail job = scheduleJob(datasetId, sourceId, mapperId);
        jobRepository.save(new PitagoraJob(job));
    }

    @Transactional
    public JobDetail scheduleJob(ObjectId datasetId, ObjectId sourceId, ObjectId mapperId) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(PitagoraJobExecutor.class)
                .usingJobData(DATASET_ID, datasetId.toString())
                .usingJobData(SOURCE_ID, sourceId.toString())
                .usingJobData(MAPPER_ID, mapperId.toString())
                .build();

        PitagoraSource source = sp.sourceService.getSourceById(sourceId);

        SimpleScheduleBuilder schedulerBuilder = simpleSchedule().repeatForever();
        switch (source.getHttpConfiguration().getUpdateRateUnit()) {
//            case SECONDS:
//                schedulerBuilder.withIntervalInSeconds(source.getHttpConfiguration().getUpdateRateValue());
//                break;
            case MINUTES:
                schedulerBuilder.withIntervalInMinutes(source.getHttpConfiguration().getUpdateRateValue());
                break;
            case HOURS:
                schedulerBuilder.withIntervalInHours(source.getHttpConfiguration().getUpdateRateValue());
                break;
            default:
                log.error("UpdateRateUnit unknown");
                throw PitagoraException.internalServerError();
        }

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(schedulerBuilder)
                .build();

        scheduler.scheduleJob(job, trigger);

        log.info("Started Job with key " + job.getKey().getName() + " for HttpSource " + source.getName());

        return job;
    }

    public void deleteByDatasetId(ObjectId datasetId) {
        delete(jobRepository.findAllByDatasetId(datasetId));
    }
    public void deleteBySourceId(ObjectId sourceId) {
        delete(jobRepository.findAllBySourceId(sourceId));
    }
    public void deleteByMapperId(ObjectId mapperId) {
        delete(jobRepository.findAllByMapperId(mapperId));
    }

    @Transactional
    public void delete(List<PitagoraJob> jobs) {
        try {

            scheduler.deleteJobs(jobs.stream().map(job -> new JobKey(job.getJobKey())).collect(Collectors.toList()));

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        jobRepository.deleteAll(jobs);
    }

    public void rescheduleJobsDueToSourceChange(ObjectId sourceId) {
        List<PitagoraJob> allJobsBySourceId = jobRepository.findAllBySourceId(sourceId);
        if (allJobsBySourceId.isEmpty()) {

            for (PitagoraDataset ds : sp.datasetService.findAllDatasetsBySourceId(sourceId)) {
                for (DatasetGenerator gen : ds.getGenerators()) {
                    if (sourceId.equals(gen.getSourceId())) {

                        try {
                            startJob(ds.getId(), sourceId, gen.getMapperId());
                        } catch (SchedulerException e) {
                            log.error("Error scheduling job");
                            throw PitagoraException.badRequest(t(ERROR_HTTPSOURCE_SCHEDULING));
                        }

                    }
                }
            }

        } else {

            _rescheduleDueToChange(allJobsBySourceId);

        }

    }
    public void rescheduleJobsDueToMapperChange(ObjectId mapperId) {
        _rescheduleDueToChange(jobRepository.findAllByMapperId(mapperId));
    }
    private void _rescheduleDueToChange(List<PitagoraJob> jobs) {

        delete(jobs);

        for (PitagoraJob job : jobs) {
            try {

                startJob(job.getDatasetId(), job.getSourceId(), job.getMapperId());

            } catch (PitagoraException e) {
                log.info("Cannot restart Job with key " + job.getJobKey());
            } catch (SchedulerException e) {
                log.error("Error scheduling Quartz Job");
                throw PitagoraException.internalServerError();
            }

        }
    }
}
