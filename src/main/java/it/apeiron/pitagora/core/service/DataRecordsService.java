package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.ERROR_HTTPSOURCE_SCHEDULING;

import com.mongodb.MongoNamespace;
import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO;
import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO.AggregatedModelDTO;
import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO.AggregatedModelFieldDTO;
import it.apeiron.pitagora.core.dto.DatasetTableDataDTO;
import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.dto.PaginationDTO;
import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import it.apeiron.pitagora.core.dto.SelectedRecordsToBeDeleted;
import it.apeiron.pitagora.core.dto.charts.FieldDataDTO;
import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.UpdateRateMode;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.util.MessagesCore;
import it.apeiron.pitagora.core.util.QueryUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@Service
@RequiredArgsConstructor
public class DataRecordsService {

    /**
     * Questa costante serve a gestire l'id del singolo record lato client:
     * per assicurarsi di evitare omonimie con i nomi dei campi del record stesso
     * il FAKE_ATTRIBUTE deve iniziare con un numero (caso non ammesso per i "veri"
     * attributi del modello)
     */
    public static final String FAKE_MODEL_ATTRIBUTE_FOR_ID = "0_recordId";
    public static final String ENRICHMENT_AGGREGATION_PREFIX = "_aggr_";

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    @Transactional
    public void create(List<Map<String, Object>> data, ObjectId datasetId) {
        PitagoraDataset ds = sp.datasetService.findById(datasetId);
        List<PitagoraData> records = data.stream()
                .map(PitagoraData::new).collect(Collectors.toList());
        records = new ArrayList<>(sp.mongoTemplateData.insert(records, ds.getName()));

        sp.alarmService.checkAlarmsOnDataset(datasetId, records, sp.alarmService.findAllByDatasetId(datasetId));
    }

    public void generateRecords(PitagoraSource source, PitagoraDataset ds, PitagoraMapper mapper) {
        List<Map<String, Object>> records;
        switch (source.getChannel()) {
            case HTTP:
                records = _createFromHttpResponse(ds.getId(), source, mapper);
                break;
            case FILE_EXCEL:
            case FILE_CSV:
                records = _createFromFile(source, mapper);
                break;
            default:
                throw PitagoraException.notAcceptable("Unknown Source Channel");
        }

        create(records, ds.getId());
    }

    @Transactional
    public List<Map<String, Object>> _createFromHttpResponse(ObjectId datasetId, PitagoraSource source, PitagoraMapper mapper) {
        if (!source.getChannel().equals(mapper.getChannel())) {
            log.error("This Mapper is not for Http Source");
            throw PitagoraException.notAcceptable(t(MessagesCore.MAPPER_NOT_HTTP));
        }

        if (UpdateRateMode.UNA_TANTUM.equals(source.getHttpConfiguration().getUpdateRateMode())) {
            HttpResponseDTO resDto = sp.httpService.getHttpSourceResponseNow(source.getHttpConfiguration());
            return sp.jsonService.generateDatasetRecords(mapper, resDto.getResponseBody());
        } else {
            try {

                sp.jobService.startJob(datasetId, source.getId(), mapper.getId());

            } catch (SchedulerException e) {
                log.error("Error scheduling job");
                throw PitagoraException.badRequest(t(ERROR_HTTPSOURCE_SCHEDULING));
            }
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> _createFromFile(PitagoraSource source, PitagoraMapper mapper) {
        if (!source.getChannel().equals(mapper.getChannel())) {
            log.error("Mapper and Source are unrelated");
            throw PitagoraException.notAcceptable(t(MessagesCore.MAPPER_AND_SOURCE_UNRELATED));
        }
        String dataBase64 = sp.sourceService.getFileById(source.getFileUploadConfiguration().getFileId()).getDataBase64();
        switch (source.getChannel()) {
            case FILE_EXCEL:
                return sp.excelService.generateDatasetRecords(mapper, dataBase64);
            case FILE_CSV:
                return sp.csvService.generateDatasetRecords(mapper, dataBase64);
            default:
                log.error("Unknown File Source Channel");
                throw PitagoraException.notAcceptable("Unknown File Source Channel");
        }
    }

    public void generateRecordsFromExposedApi(String apiKey, List<Map<String, Object>> dataRecords) {
        PitagoraSource src = sp.sourceService.getSourceByApiKey(apiKey);
        PitagoraModel model = sp.modelService.getDatasetModel(src.getExposedApiConfiguration().getDatasetId());

        dataRecords.forEach(record -> {
            if (!model.getStructure().keySet().containsAll(record.keySet())) {
                throw PitagoraException.badRequest("Submitted data contain one or more fields unknown to the Dataset");
            }
        });
        create(dataRecords, src.getExposedApiConfiguration().getDatasetId());
    }

    public DatasetTableDataDTO getPagedRecordsByQuery(QueryDatasetDTO query) {
        PitagoraDataset ds = sp.datasetService.findById(new ObjectId(query.getDatasetId()));
        PitagoraModel model = sp.modelService.getModelById(ds.getModelId());

        PaginationDTO p = query.getPage();
        String sortBy = "data." + p.getSortBy();
        Pageable pageable = PageRequest.of(
                p.getNumber(),
                p.getSize(),
                Direction.ASC.name().toLowerCase().equals(p.getSortDirection()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());

        Query q = new Query().with(pageable);

        List<PitagoraData> results = _search(ds, query, q);
        Page<PitagoraData> pagedData = PageableExecutionUtils.getPage(
                results,
                pageable,
                () -> sp.mongoTemplateData.count(Query.of(q).limit(-1).skip(-1), PitagoraData.class, ds.getName()));

        List<Map<String, Object>> data = pagedData.getContent().stream().map(pitagoraData -> {
            Map<String, Object> record = new HashMap<>(pitagoraData.getData());
            record.put(FAKE_MODEL_ATTRIBUTE_FOR_ID, pitagoraData.getId());
            return record;
        })
                .collect(Collectors.toList());

        return DatasetTableDataDTO.builder()
                .data(data)
                .fields(new ArrayList<>(model.getStructure().values()))
                .page(PaginationDTO.builder()
                        .number(pagedData.getNumber())
                        .size(pagedData.getSize())
                        .total(pagedData.getTotalPages())
                        .sortBy(query.getPage().getSortBy())
                        .sortDirection(query.getPage().getSortDirection())
                        .build())
                .datasetName(ds.getNameForClient())
                .build();
    }

    public DatasetTableDataDTO getAllRecordsByQuery(QueryDatasetDTO query) {
        PitagoraDataset ds = sp.datasetService.findById(new ObjectId(query.getDatasetId()));
        PitagoraModel model = sp.modelService.getModelById(ds.getModelId());

        Query q = new Query();

        List<Map<String, Object>> data = _search(ds, query, q).stream()
                .map(PitagoraData::getData).collect(Collectors.toList());

        return DatasetTableDataDTO.builder()
                .data(data)
                .fields(new ArrayList<>(model.getStructure().values()))
                .datasetName(ds.getName())
                .build();
    }

    private List<PitagoraData> _search(PitagoraDataset dataset, QueryDatasetDTO queryDTO, Query query) {
        if (!sp.jwtService.userHasRightsOn(dataset)) {
            return Collections.emptyList();
        }
        _addCriteriaFromFiltersToQuery(query, queryDTO);
        return sp.mongoTemplateData.find(query, PitagoraData.class, dataset.getName());
    }

    private void _addCriteriaFromFiltersToQuery(Query query, QueryDatasetDTO queryDTO) {
        if ((queryDTO.getFilters() != null && !queryDTO.getFilters().isEmpty()) || queryDTO.getStartingDate() != null || queryDTO.getEndingDate() != null) {
            Criteria criteriaDefinition = _buildCriteriaDefinition(queryDTO.getFilters(), "", queryDTO.getStartingDate(), queryDTO.getEndingDate());
            if (criteriaDefinition != null) {
                query.addCriteria(criteriaDefinition);
            }
        }
    }

    private Criteria _buildCriteriaDefinition(List<Filter> filters, String wherePrefix, LocalDateTime startingDate, LocalDateTime endingDate) {
        List<Criteria> allCriteria = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            allCriteria = filters.stream().map(filter -> QueryUtils.buildQueryCriteria(filter, wherePrefix + "data." + filter.getField()))
                    .collect(Collectors.toList());
        }

        if (startingDate != null) {
            allCriteria.add(Criteria.where(wherePrefix + "createdAt").gte(startingDate));
        }
        if (endingDate != null) {
            allCriteria.add(Criteria.where(wherePrefix + "createdAt").lte(endingDate));
        }

        if (allCriteria.isEmpty()) {
            return null;
        }
        return new Criteria().andOperator(allCriteria);
    }

    public Map<String, FieldDataDTO> getDataRecordsAsColumnsByDatasetId(QueryDatasetDTO query, String columnNameX, String columnNameY) {

        DatasetTableDataDTO datasetTableData = getAllRecordsByQuery(query);

        Map<String, FieldDataDTO> fieldsMaps = new HashMap<>();

        datasetTableData.getFields().forEach(model -> {
            if (model.getName().equals(columnNameX) || model.getName().equals(columnNameY)) {
                fieldsMaps.put(model.getName(), new FieldDataDTO(model, datasetTableData.getData().stream().map(d -> d.get(model.getName())).collect(Collectors.toList())));
            }
        });

        return fieldsMaps;
    }

    @Transactional
    public Integer deleteRecordsByQuery(QueryDatasetDTO query) {
        if (query.getFilters().isEmpty() && query.getStartingDate() == null && query.getEndingDate() == null) {
            return 0;
        }
        PitagoraDataset ds = sp.datasetService.findById(new ObjectId(query.getDatasetId()));
        Query q = new Query();
        List<PitagoraData> records = _search(ds, query, q);

        if (query.isDelete()) {
            remove(q, ds.getName());
        }
        return records.size();
    }

    public void remove(Query q, String collection) {
        sp.mongoTemplateData.remove(q, collection);
        if (countAllByDataset(collection) <= 0) {
            dropCollection(collection);
        }
    }

    public long countAllByDataset(String datasetName) {
        return sp.mongoTemplateData.count(new Query(), datasetName);
    }

    @Transactional
    public void dropCollection(String datasetName) {
        sp.mongoTemplateData.dropCollection(datasetName);
    }

    public List<PitagoraData> findAllByDataset(ObjectId datasetId) {
        return _search(sp.datasetService.findById(datasetId), QueryDatasetDTO.builder().build(), new Query());
    }

    @Transactional
    public void renameDatasetCollection(String oldName, String newName) {
        sp.mongoTemplateData.getCollection(oldName).renameCollection(new MongoNamespace("pitagora_data", newName));
    }

    @Transactional
    public void deleteSelectedRecordsByDatasetId(SelectedRecordsToBeDeleted data) {
        if (data.getRecordsId().isEmpty()) {
            return;
        }

        PitagoraDataset ds = sp.datasetService.findById(new ObjectId(data.getDatasetId()));
        Query q = new Query();
        List<ObjectId> ids = data.getRecordsId().stream().map(ObjectId::new).collect(Collectors.toList());
        q.addCriteria(Criteria.where("_id").in(ids));

        remove(q, ds.getName());
    }

    @Transactional
    public String enrich(DatasetEnrichmentDTO dto) {
        PitagoraDataset firstDs = sp.datasetService.findDatasetByIdWithAggregations(dto.getFirstDatasetId());
        PitagoraDataset secondDs = sp.datasetService.findDatasetByIdWithAggregations(dto.getSecondDatasetId());

        PitagoraModel model = sp.modelService.createAggregatedModel(dto.getModelName(), dto.getModelStructure());
        PitagoraDataset dataset = sp.datasetService.createAggregatedDataset(dto.getDatasetName(), model.getId());

        log.info("Enrichment of [" + firstDs.getName() + "] with [" + secondDs.getName() + "] ...");
        List<Map<String, Object>> enrichedRecords = _generateEnrichedRecords(dto, firstDs, secondDs);
        create(enrichedRecords, dataset.getId());
        log.info("Enrichment done: " + enrichedRecords.size() + " records created on Dataset [" + dataset.getName() + "]");
        return dataset.getId().toString();
    }

    private List<Map<String, Object>> _generateEnrichedRecords(DatasetEnrichmentDTO dto, PitagoraDataset firstDs, PitagoraDataset secondDs) {
        List<AggregationOperation> aggregationsOps = new ArrayList<>();
        aggregationsOps.add(Aggregation.lookup(secondDs.getName(), "data." + dto.getFirstDatasetField(), "data." + dto.getSecondDatasetField(), "aggregations"));

        Criteria criteria = _buildCriteriaDefinition(dto.getFirstDatasetFilters(), "",
                dto.getFirstDatasetStartingDate(), dto.getFirstDatasetEndingDate());
        if (criteria != null) {
            aggregationsOps.add(Aggregation.match(criteria));
        }

        Criteria secondCriteria = _buildCriteriaDefinition(dto.getSecondDatasetFilters(), "aggregations.",
                dto.getSecondDatasetStartingDate(), dto.getSecondDatasetEndingDate());
        if (secondCriteria != null) {
            aggregationsOps.add(Aggregation.match(secondCriteria));
        }

        List<PitagoraData> mappedResults = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsOps), firstDs.getName(), PitagoraData.class)
                .getMappedResults();

        return extractEnrichedRecordsFromLookupResults(mappedResults, dto.getModelStructure());
    }

    public List<Map<String, Object>> extractEnrichedRecordsFromLookupResults(List<PitagoraData> wholeData, AggregatedModelDTO aggregatedModelDTO) {
        List<Map<String, Object>> records = new ArrayList<>();

        wholeData.forEach(r -> {
            Map<String, Object> mainData = new HashMap<>();
            aggregatedModelDTO.getFirstDataset()
                    .forEach(fd -> mainData.put(fd.getTo().getName(), r.getData().get(fd.getFrom().getName())));
            if (r.getAggregations().isEmpty()) {
                r.getAggregations().add(new PitagoraData());
            }
            r.getAggregations().forEach(aggData ->
                    {
                        records.add(_extractEnrichedRecord(mainData, aggData.getData(),
                                aggregatedModelDTO.getSecondDataset()));
                    }
            );
        });
        return records;
    }

    private Map<String, Object> _extractEnrichedRecord(Map<String, Object> mainData, Map<String, Object> aggregationData,
            Collection<AggregatedModelFieldDTO> secondDatasetAggregatedFields) {
        Map<String, Object> enrichedRecord = new HashMap<>(mainData);
        secondDatasetAggregatedFields.forEach(sd ->
                enrichedRecord.put(sd.getTo().getName(), aggregationData.get(sd.getFrom().getName()))
        );
        return enrichedRecord;
    }
}
