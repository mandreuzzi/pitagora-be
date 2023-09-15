package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.ALARMS_HAVE_BEEN;
import static it.apeiron.pitagora.core.util.MessagesCore.DISABLED;
import static it.apeiron.pitagora.core.util.MessagesCore.ENABLED;

import it.apeiron.pitagora.core.dto.AlarmDTO;
import it.apeiron.pitagora.core.dto.DatasetAlarmsDetailDTO;
import it.apeiron.pitagora.core.dto.EventCountDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraAlarm;
import it.apeiron.pitagora.core.entity.collection.PitagoraAlarmEvent;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.NotificationChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraAlarmRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@Service
@RequiredArgsConstructor
public class AlarmService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraAlarmRepository alarmRepository;


    public List<AlarmDTO> getAlarmsByDatasetId(ObjectId datasetId) {
        return alarmRepository
                .findAllByDatasetId(datasetId).stream().map(a -> AlarmDTO.fromModel(a)).collect(Collectors.toList());
    }

    @Transactional
    public void setAlarmsOnDataset(ObjectId datasetId, List<AlarmDTO> dtos) {
        PitagoraDataset ds = sp.datasetService.findById(datasetId);

        List<PitagoraAlarm> olds = alarmRepository.findAllByDatasetId(datasetId);
        List<String> current = dtos.stream().map(AlarmDTO::getId).collect(Collectors.toList());
        List<PitagoraAlarm> toBeDeleted = olds.stream().filter(old -> !current.contains(old.getId().toString())).collect(Collectors.toList());
        alarmRepository.deleteAll(toBeDeleted);

        dtos.forEach(dto -> {
            PitagoraAlarm toBeSaved;
            if (StringUtils.isNotEmpty(dto.getId())) {
                toBeSaved = alarmRepository.findById(new ObjectId(dto.getId()))
                        .orElseThrow(() -> PitagoraException.badRequest("Alarm with id " + dto.getId() + " not found"));
                toBeSaved.update(dto);
            } else {
                toBeSaved = new PitagoraAlarm(datasetId, ds.getScope(), dto);
            }
            alarmRepository.save(toBeSaved);
        });

        log.info("Alarms on Dataset " + ds.getName() + " set");
    }


    public List<DatasetAlarmsDetailDTO> getAllDatasetAlarmsDetail() {
        List<ValueDescriptionDTO> allDatasets = sp.datasetService.getAllDatasets();

        return allDatasets.stream().map(ds -> {

            DatasetAlarmsDetailDTO dto = new DatasetAlarmsDetailDTO(ds.getValue(), ds.getDescription());
            List<PitagoraAlarm> alarms = alarmRepository.findAllByDatasetId(new ObjectId(ds.getValue()));
            dto.setNotification(alarms.stream()
                    .filter(alarm -> !NotificationChannel.NULL.equals(alarm.getNotification()))
                    .map(alarm -> alarm.getNotification().getDescription()).distinct().collect(Collectors.joining(" , ")));

            int enabled = (int) alarms.stream().filter(PitagoraAlarm::isEnabled).count();
            dto.setEnabledOnTotal(enabled + "/" + alarms.size());
            dto.setLastEvent(_getLastEventTime(alarms));

            return dto;
        }).collect(Collectors.toList());
    }

    private Long _getLastEventTime(List<PitagoraAlarm> alarms) {
        List<LocalDateTime> allEvents = new ArrayList<LocalDateTime>();

        for (PitagoraAlarm alarm : alarms) {
            Optional<PitagoraAlarmEvent> result = sp.alarmEventService.findMostRecentEventOnAlarm(alarm.getId());
            result.ifPresent(pitagoraAlarmEvent -> allEvents.add(pitagoraAlarmEvent.getCreatedAt()));
        }

        if (allEvents.isEmpty()) {
            return null;
        }

        return allEvents.stream().max(LocalDateTime::compareTo).get().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Transactional
    public String changeAlarmsStatus(ObjectId datasetId) {
        boolean result = !alarmRepository.existsByEnabledAndDatasetId(true, datasetId);
        Query query = new Query();
        query.addCriteria(Criteria.where("datasetId").is(datasetId));
        Update update = new Update();
        update.set("enabled", result);
        sp.mongoTemplate.updateMulti(query, update, PitagoraAlarm.class);

        return t(ALARMS_HAVE_BEEN) + (result ? t(ENABLED) : t(DISABLED)).toUpperCase();
    }

    public int countAllByDatasetId(ObjectId datasetId) {
        return alarmRepository.countAllByDatasetId(datasetId);
    }

    public void deleteAllByDatasetId(ObjectId datasetId) {
        alarmRepository.deleteAllByDatasetId(datasetId);
    }

    public PitagoraAlarm findById(ObjectId alarmId) {
        return alarmRepository.findById(alarmId)
                .orElseThrow(() -> PitagoraException
                        .notAcceptable("Alarm with id '" + alarmId + "' not found"));
    }

    public List<PitagoraAlarm> findAllByDatasetId(ObjectId datasetId) {
        return alarmRepository.findAllByDatasetId(datasetId);
    }

    public EventCountDTO checkAlarmsOnDataset(ObjectId datasetId, List<PitagoraData> dataList, List<PitagoraAlarm> allAlarms) {

        EventCountDTO events = new EventCountDTO();
        String datasetName = this.sp.datasetService.findById(datasetId).getName();
        events.setDatasetName(datasetName);

        List<PitagoraAlarm> allActiveAlarms = allAlarms.stream().filter(PitagoraAlarm::isEnabled).collect(Collectors.toList());
        if (allActiveAlarms.isEmpty()) {
            return events;
        }

        HashMap<ObjectId, Integer> eventCountPerAlarms = new HashMap<>();//TODO: fix cambiare la mappa da <string, int> a <Alarm, int>
        AtomicInteger eventsCounter = new AtomicInteger(0);



        dataList.forEach(dataRecord -> {

            List<PitagoraAlarm> triggeredAlarms = _checkAlarms(dataRecord, allAlarms);
            if (!triggeredAlarms.isEmpty()) {
                eventsCounter.addAndGet(triggeredAlarms.size());

                triggeredAlarms.forEach(alarm -> {
                    if (eventCountPerAlarms.containsKey(alarm.getId())) {
                        eventCountPerAlarms.put(alarm.getId(), eventCountPerAlarms.get(alarm.getId()) + 1);
                    } else {
                        eventCountPerAlarms.put(alarm.getId(), 1);
                    }
                });
            }
        });
        events.setTotalCount(eventsCounter.get());
        eventCountPerAlarms.keySet().forEach(alarmId -> {
            PitagoraAlarm currentAlarm = allAlarms.stream().filter(alarm -> alarm.getId().equals(alarmId)).findFirst().get();
            events.getAlarms().add(new ValueDescriptionDTO(eventCountPerAlarms.get(alarmId).toString(), currentAlarm.getName()));
        });

        if (allActiveAlarms.stream().anyMatch(alarm -> !NotificationChannel.NULL.equals(alarm.getNotification()))) {
            sp.notificationService.notifyAlarms(eventCountPerAlarms, allAlarms, datasetName);
        }

        return events;
    }

    private List<PitagoraAlarm> _checkAlarms(PitagoraData dataRecord, List<PitagoraAlarm> alarms) {
        AtomicBoolean atLeastOneEvent = new AtomicBoolean(false);
        List<PitagoraAlarm> triggeredAlarms = new ArrayList<>(); //Returnare lista Di allarmi
        alarms.stream()
                .filter(PitagoraAlarm::isEnabled)
                .forEach(alarm -> {
                    Map<String, Object> triggerValues = new HashMap<>();
                    alarm.getConditions().forEach(cond -> {
                                boolean conditionSatisfied = sp.alarmEventService.checkConditionToTriggerEvent(cond, dataRecord.getData().get(cond.getField()));
                                if (conditionSatisfied) {
                                    triggerValues.put(cond.getField(), dataRecord.getData().get(cond.getField()));

                                }
                            }
                    );
                    boolean eventMustBeTriggered = triggerValues.size() == alarm.getConditions().size();
                    if (eventMustBeTriggered) {
                        atLeastOneEvent.set(true);
                        sp.alarmEventService.save(PitagoraAlarmEvent.builder()
                                .datasetId(alarm.getDatasetId())
                                .alarmId(alarm.getId())
                                .eventTime(dataRecord.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                                .triggerValues(triggerValues)
                                .build());

                        triggeredAlarms.add(alarm);
                    }
                });
        return triggeredAlarms;
    }

    public EventCountDTO checkAlarmOnDataset(ObjectId alarmId) {
        PitagoraAlarm alarm = sp.alarmService.findById(alarmId);
        return checkAlarmsOnDataset(alarm.getDatasetId(), sp.dataRecordsService.findAllByDataset(alarm.getDatasetId()), Collections.singletonList(alarm));
    }

    public EventCountDTO checkAllAlarmsOnDataset(ObjectId datasetId) {
        return checkAlarmsOnDataset(datasetId, sp.dataRecordsService.findAllByDataset(datasetId), sp.alarmService.findAllByDatasetId(datasetId));
    }

}
