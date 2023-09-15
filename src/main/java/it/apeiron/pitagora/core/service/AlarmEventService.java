package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.entity.enums.FieldType.STRING;
import static it.apeiron.pitagora.core.entity.enums.FieldType.TIMESTAMP;

import it.apeiron.pitagora.core.dto.AlarmEventDTO;
import it.apeiron.pitagora.core.dto.AlarmEventTableDTO;
import it.apeiron.pitagora.core.dto.AlarmsSeverityInfoDTO;
import it.apeiron.pitagora.core.dto.PaginationDTO;
import it.apeiron.pitagora.core.dto.QueryEventsDTO;
import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.collection.PitagoraAlarm.AlarmCondition;
import it.apeiron.pitagora.core.entity.collection.PitagoraAlarmEvent;
import it.apeiron.pitagora.core.entity.enums.AlarmSeverity;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraAlarmEventRepository;
import it.apeiron.pitagora.core.util.QueryUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@Service
@RequiredArgsConstructor
public class AlarmEventService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraAlarmEventRepository alarmEventRepository;

    private final SimpleDateFormat sdf;

    public boolean checkConditionToTriggerEvent(AlarmCondition condition, Object recordValue) {
        Operation op = condition.getOperation();
        Object alarmValue = condition.getValue();
        if (TIMESTAMP.equals(condition.getFieldType())) {
            alarmValue = _parseTimestamp(condition.getValue());
        }

        String alarmString = String.valueOf(alarmValue);
        String recordString = String.valueOf(recordValue);

        if (Operation.EQUALS.equals(op)) {
            if (STRING.equals(condition.getFieldType())) {
                return (alarmString).equalsIgnoreCase(recordString);
            }
            return recordValue == alarmValue;
        } else if (Operation.NOT_EQUALS.equals(op)) {
            if (STRING.equals(condition.getFieldType())) {
                return !((alarmString).equalsIgnoreCase(recordString));
            }
            return recordValue != alarmValue;
        } else if (Operation.CONTAINS.equals(op)) {
            return recordString.toLowerCase().contains((alarmString.toLowerCase()));
        } else if (Operation.NOT_CONTAINS.equals(op)) {
            return !(recordString.toLowerCase().contains((alarmString.toLowerCase())));
        } else if (Operation.IS_NULL.equals(op)) {
            return Objects.isNull(recordValue);
        } else if (Operation.IS_TRUE.equals(op)) {
            return (boolean) recordValue;
        } else if (Operation.IS_FALSE.equals(op)) {
            return !((boolean) recordValue);
        } else if (Operation.NOT_IS_NULL.equals(op)) {
            return Objects.nonNull(recordValue);
        } else if (Operation.IS_BLANK.equals(op)) {
            return StringUtils.isBlank((String) recordValue);
        } else if (Operation.NOT_IS_BLANK.equals(op)) {
            return StringUtils.isNotBlank((String) recordValue);
        } else {
            if (recordValue == null) {
                return false;
            }
            switch (op) {
                case GT:
                    return TIMESTAMP.equals(condition.getFieldType()) ?
                            Long.parseLong(recordString) > Long.parseLong(alarmString) : Double.parseDouble(recordString) > Double.parseDouble(alarmString);
                case GTE:
                    return TIMESTAMP.equals(condition.getFieldType()) ?
                            Long.parseLong(recordString) >= Long.parseLong(alarmString) : Double.parseDouble(recordString) >= Double.parseDouble(alarmString);
                case LT:
                    return TIMESTAMP.equals(condition.getFieldType()) ?
                            Long.parseLong(recordString) < Long.parseLong(alarmString) : Double.parseDouble(recordString) < Double.parseDouble(alarmString);
                case LTE:
                    return TIMESTAMP.equals(condition.getFieldType()) ?
                            Long.parseLong(recordString) <= Long.parseLong(alarmString) : Double.parseDouble(recordString) <= Double.parseDouble(alarmString);
            }
        }

        return false;
    }

    private Long _parseTimestamp(String dateString) {
        try {
            return dateString != null ? sdf.parse(dateString).toInstant().toEpochMilli() : null;
        } catch (Exception e) {
            throw PitagoraException.internalServerError();
        }
    }

    public AlarmEventTableDTO getPagedEventsByQuery(QueryEventsDTO query) {
        PaginationDTO p = query.getPage();

        String sortByReq = p.getSortBy();
        Sort sort;
        if ("datasetName".equals(sortByReq)) {
            sort = Sort.by("dataset.name");
        } else if ("alarmName".equals(sortByReq)) {
            sort = Sort.by("alarm.name");
        } else if ("severity".equals(sortByReq)) {
            sort = Sort.by("alarm.severity");
        } else {
            sort = Sort.by("eventTime");
        }
        sort = Direction.ASC.name().toLowerCase().equals(query.getPage().getSortDirection()) ? sort.ascending() : sort.descending();

//        Pageable pageable = PageRequest.of(p.getNumber(), p.getSize(), sort);
//        Query q = new Query().with(pageable);

        List<AggregationOperation> aggregationsOps = new ArrayList<>();
        aggregationsOps.add(Aggregation.lookup("alarm", "alarmId", "_id", "alarm"));
        aggregationsOps.add(Aggregation.lookup("dataset", "datasetId", "_id", "dataset"));

        if (!query.getFilters().isEmpty()) {
            Criteria criteria = new Criteria().andOperator(query.getFilters().stream()
                    .map(filter ->
                            TIMESTAMP.equals(filter.getFieldType()) ?
                                    QueryUtils.buildQueryCriteria(filter, "eventTime")
                                    : ( filter.getField().equals("severity") ? QueryUtils.buildQueryCriteria(filter, "alarm.severity") : Criteria.where(filter.getField()).is(new ObjectId(filter.getValue())))
                    ).collect(Collectors.toList()));

//            q.addCriteria(criteria);
            aggregationsOps.add(Aggregation.match(criteria));
        }

        aggregationsOps.add(Aggregation.sort(sort));

        List<PitagoraAlarmEvent> results = sp.mongoTemplate.aggregate(Aggregation.newAggregation(aggregationsOps), PitagoraAlarmEvent.class, PitagoraAlarmEvent.class).getMappedResults();

        int totalPages = _totalPages(results, p.getSize());
        double maxNumOfPages = Math.ceil((double) results.size() / p.getSize());
        int currPageNumber = (int) Math.min(p.getNumber(), maxNumOfPages);
        p.setNumber(currPageNumber);
        List<PitagoraAlarmEvent> paginated = _paginate(results, p.getNumber(), p.getSize());
        List<AlarmEventDTO> data = _buildDTOs(paginated);

        return AlarmEventTableDTO.builder()
                .data(data)
                .page(PaginationDTO.builder()
                        .number(p.getNumber())
                        .size(p.getSize())
                        .total(totalPages)
                        .sortBy(query.getPage().getSortBy())
                        .sortDirection(query.getPage().getSortDirection())
                        .build())
                .build();
    }

    private int _totalPages(List<PitagoraAlarmEvent> allEvents, int pageSize) {
        return allEvents.size() / pageSize + (allEvents.size() % pageSize == 0 ? 0 : 1);
    }

    private List<PitagoraAlarmEvent> _paginate(List<PitagoraAlarmEvent> allEvents, int pageNumber, int pageSize) {
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, allEvents.size());
        return allEvents.subList(start, end);
    }

    private List<AlarmEventDTO> _buildDTOs(List<PitagoraAlarmEvent> allEvents) {
        return allEvents.stream().map(event -> AlarmEventDTO.builder()
                .datasetName(event.getDataset().getNameForClient())
                .alarmName(event.getAlarm().getNameForClient())
                .severity(event.getAlarm().getSeverity().toString())
                .eventTime(new Date(event.getEventTime()).toInstant().toEpochMilli())
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public PitagoraAlarmEvent save(PitagoraAlarmEvent event) {
        return alarmEventRepository.save(event);
    }

    public int countAllByDatasetId(ObjectId datasetId) {
        return alarmEventRepository.countAllByDatasetId(datasetId);
    }

    @Transactional
    public void deleteAllByDatasetId(ObjectId datasetId) {
        alarmEventRepository.deleteAllByDatasetId(datasetId);
    }

    public Optional<PitagoraAlarmEvent> findMostRecentEventOnAlarm(ObjectId alarmId) {
        return alarmEventRepository.findFirstByAlarmIdOrderByCreatedAtDesc(alarmId);
    }

    public AlarmsSeverityInfoDTO getAlarmsSeverity() {
        return new AlarmsSeverityInfoDTO(
                _countEventByAlarmSeverity(AlarmSeverity.HIGH),
                _countEventByAlarmSeverity(AlarmSeverity.MEDIUM),
                _countEventByAlarmSeverity(AlarmSeverity.LOW)
        );
    }

    private int _countEventByAlarmSeverity(AlarmSeverity severity) {
        List<AggregationOperation> aggregationsOps = new ArrayList<>();
        aggregationsOps.add(Aggregation.lookup("alarm", "alarmId", "_id", "alarm"));
        aggregationsOps.add(Aggregation.match(
                QueryUtils.buildQueryCriteria(
                        Filter.builder().fieldType(STRING).value(severity.name()).operation(Operation.EQUALS).build(),
                        "alarm.severity"))
        );
        List<PitagoraAlarmEvent> results = sp.mongoTemplate.aggregate(Aggregation.newAggregation(aggregationsOps), PitagoraAlarmEvent.class, PitagoraAlarmEvent.class).getMappedResults();
        return results.size();
    }
}
