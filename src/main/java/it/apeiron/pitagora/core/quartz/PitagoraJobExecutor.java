package it.apeiron.pitagora.core.quartz;

import static it.apeiron.pitagora.core.quartz.JobService.DATASET_ID;
import static it.apeiron.pitagora.core.quartz.JobService.MAPPER_ID;
import static it.apeiron.pitagora.core.quartz.JobService.SOURCE_ID;

import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.service.DataRecordsService;
import it.apeiron.pitagora.core.service.HttpService;
import it.apeiron.pitagora.core.service.JsonService;
import it.apeiron.pitagora.core.service.MapperService;
import it.apeiron.pitagora.core.service.SourceService;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@CommonsLog
@Component
public class PitagoraJobExecutor implements Job {

    @Autowired
    private HttpService httpService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private MapperService mapperService;
    @Autowired
    private JsonService jsonService;
    @Autowired
    private DataRecordsService dataRecordsService;

    public void execute(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String datasetId = dataMap.getString(DATASET_ID);
        String sourceId = dataMap.getString(SOURCE_ID);
        String mapperId = dataMap.getString(MAPPER_ID);

        PitagoraSource source = sourceService.getSourceById(new ObjectId(sourceId));
        HttpResponseDTO resDto = httpService.getHttpSourceResponseNow(source.getHttpConfiguration());
        String size = "0";
        if (resDto.getStatusCode() >= 200 && resDto.getStatusCode() < 300) {

            PitagoraMapper mapper = mapperService.getMapperById(new ObjectId(mapperId));
            List<Map<String, Object>> records = jsonService.generateDatasetRecords(mapper, resDto.getResponseBody());

            dataRecordsService.create(records, new ObjectId(datasetId));
            size = Integer.toString(records.size());
        }

        log.debug("Executed Job with for HttpSource with id " + sourceId + ": " + size + " records added on Dataset with id " + datasetId);
    }
}
