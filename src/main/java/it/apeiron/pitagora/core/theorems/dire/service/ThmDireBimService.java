package it.apeiron.pitagora.core.theorems.dire.service;

import static it.apeiron.pitagora.core.util.QueryUtils.sdf;

import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import it.apeiron.pitagora.core.theorems.dire.dto.BimDetailsRequestDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.MaintenanceRequestDTO;
import it.apeiron.pitagora.core.util.QueryUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmDireBimService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }


    public Map getBimDetails(BimDetailsRequestDTO dto) {
        List<AggregationOperation> yearsAndAddressesFilters = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(new MaintenanceRequestDTO(dto.getYears(), List.of(dto.getSite())), List.of("reviewedAt", "dataIntervento"));
        Map<String, Object> details = new HashMap();
        _getMostRecentExecuted(dto, details);
        _getNextScheduled(dto, details);

        return details;
//        return BimDetailsResponseDTO.builder()
//                .mostRecentExecuted(details)
//                .build();
    }


    private void _getMostRecentExecuted(BimDetailsRequestDTO dto, Map<String, Object> details) {

        List<AggregationOperation> aggregationsPipeline = new ArrayList<>();

        Filter reviewStatusFilter = Filter.builder().field("reviewStatus").fieldType(FieldType.STRING)
                .operation(Operation.EQUALS).value("Attivita eseguita").build();

        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(
                QueryUtils.buildQueryCriteria(Filter.builder().fieldType(FieldType.STRING).operation(Operation.EQUALS).field("sede").value(dto.getSite()).build(), "data.sede"),
                Criteria.where("data.dataIntervento").ne(null),
                QueryUtils.buildQueryCriteria(reviewStatusFilter, "data." + reviewStatusFilter.getField())
        )));

        aggregationsPipeline.add(Aggregation.sort(Sort.by(Direction.DESC, "data.dataIntervento")));

        List<PitagoraData> mappedResults = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName(), PitagoraData.class)
                .getMappedResults();
        if (mappedResults.isEmpty()) {
            return;
        }

        Map<String, Object> record = mappedResults.get(0).getData();
        details.put("Manutenzione Programmata più recente",
                sdf.format(new Date((long) record.get("dataIntervento"))).substring(0, 10) + "(" + record.get("intervento") + ")");
    }

    private void _getNextScheduled(BimDetailsRequestDTO dto, Map<String, Object> details) {

        List<AggregationOperation> aggregationsPipeline = new ArrayList<>();

        Filter reviewStatusFilter = Filter.builder().field("reviewStatus").fieldType(FieldType.STRING)
                .operation(Operation.EQUALS).value("Attivita programmata").build();

        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(
                QueryUtils.buildQueryCriteria(Filter.builder().fieldType(FieldType.STRING).operation(Operation.EQUALS).field("sede").value(dto.getSite()).build(), "data.sede"),
                Criteria.where("data.dataIntervento").ne(null),
                QueryUtils.buildQueryCriteria(Filter.builder().fieldType(FieldType.TIMESTAMP).operation(Operation.GT).field("dataIntervento").value(
                        sdf.format(new Date())).build(), "data.dataIntervento"),
                QueryUtils.buildQueryCriteria(reviewStatusFilter, "data." + reviewStatusFilter.getField())
        )));

        aggregationsPipeline.add(Aggregation.sort(Sort.by(Direction.ASC, "data.dataIntervento")));

        List<PitagoraData> mappedResults = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName(), PitagoraData.class)
                .getMappedResults();
        if (mappedResults.isEmpty()) {
            return;
        }

        Map<String, Object> record = mappedResults.get(0).getData();
        details.put("Prossima Manutenzione Programmata: ", sdf.format(new Date((long) record.get("dataIntervento")))
                .substring(0, 10));
    }
}
