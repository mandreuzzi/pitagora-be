package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.EXPORTED_ON;

import it.apeiron.pitagora.core.dto.DatasetCreationDTO;
import it.apeiron.pitagora.core.dto.DatasetDetailsDTO;
import it.apeiron.pitagora.core.dto.DatasetDetailsDTO.DatasetGeneratorDTO;
import it.apeiron.pitagora.core.dto.DatasetTableDataDTO;
import it.apeiron.pitagora.core.dto.FileDTO;
import it.apeiron.pitagora.core.dto.ModelDTO;
import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import it.apeiron.pitagora.core.dto.ResourceToBeDeletedDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.ExportFormat;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraDatasetRepository;
import it.apeiron.pitagora.core.util.EncodingUtils;
import it.apeiron.pitagora.core.util.MessagesCore;
import it.apeiron.pitagora.core.util.QueryUtils;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@RequiredArgsConstructor
@Service
public class DatasetService {

    private final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final String CSV_CONTENT_TYPE = "text/csv";

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraDatasetRepository datasetRepository;


    @Transactional
    public PitagoraDataset createDataset(DatasetCreationDTO dto) {
        checkDatasetNameForCreation(dto.getName());
        PitagoraDataset ds = datasetRepository.save(new PitagoraDataset(dto));
        handleGenerator(ds, dto.getSourceId(), dto.getMapperId());

        log.info("Dataset " + dto.getName() + " created");
        return ds;
    }

    public void checkDatasetNameForCreation(String name) {
        if (datasetRepository.existsByName(name)) {
            throw PitagoraException.nameNotAvailable();
        }
        _checkNameValidity(name);
    }

    @Transactional
    public void update(DatasetCreationDTO dto) {
        PitagoraDataset ds = findById(new ObjectId(dto.getId()));
        if (datasetRepository.existsByName(dto.getName()) && !dto.getName().equals(ds.getName())) {
            throw PitagoraException.nameNotAvailable();
        }
        _checkNameValidity(dto.getName());
        if (!ds.getNameForClient().equals(dto.getName())) {
            sp.dataRecordsService.renameDatasetCollection(ds.getName(), dto.getName());
        }

        if (!ds.isLocked()) {
            ds.update(dto);
            datasetRepository.save(ds);
        }
        handleGenerator(ds, dto.getSourceId(), dto.getMapperId());

        log.info("Dataset " + dto.getName() + " updated");
    }

    private void _checkNameValidity(String name) {
        if (name.contains("$") || name.startsWith("system")) {
            throw PitagoraException.notAcceptable(t(MessagesCore.DATASET_NAME_NOT_VALID));
        }
    }
    @Transactional
    public void handleGenerator(PitagoraDataset ds, String sourceId, String mapperId) {
        if (StringUtils.isEmpty(sourceId) || StringUtils.isEmpty(mapperId)) {
            return;
        }
        PitagoraSource source = sp.sourceService.getSourceById(new ObjectId(sourceId));
        PitagoraMapper mapper = sp.mapperService.getMapperById(new ObjectId(mapperId));
        if (!mapper.getReferenceSourceId().equals(source.getId())) {
            log.error("Mapper and Source are unrelated");
            throw PitagoraException.notAcceptable(t(MessagesCore.MAPPER_AND_SOURCE_UNRELATED));
        }
        if (!mapper.getReferenceModelId().equals(ds.getModelId())) {
            log.error("Mapper and Dataset's Model mismatch");
            throw PitagoraException.notAcceptable(t(MessagesCore.MAPPER_AND_DATASET_MODEL_MISMATCH));
        }
        ds.getGenerators().add(PitagoraDataset.DatasetGenerator.create(sourceId, mapperId));
        datasetRepository.save(ds);
        sp.dataRecordsService.generateRecords(source, ds, mapper);
    }

    public List<ValueDescriptionDTO> getAllDatasets() {
        return this.datasetRepository.findAll().stream()
                .filter(ds -> sp.jwtService.userHasRightsOn(ds))
                .map(ValueDescriptionDTO::fromResource).collect(Collectors.toList());
    }

    public PitagoraDataset findById(ObjectId datasetId) {
        return datasetRepository.findById(datasetId)
                .orElseThrow(() -> PitagoraException.notAcceptable("Dataset with id '" + datasetId + "' not found"));
    }

    public List<PitagoraDataset> findAll(){
        return datasetRepository.findAll();
    }
    public void delete(ObjectId datasetId) {
        handleLinkedResources(datasetId, true);
    }

    public ResourceToBeDeletedDTO getLinkedResources(ObjectId datasetId) {
        return handleLinkedResources(datasetId, false);
    }

    @Transactional
    public ResourceToBeDeletedDTO handleLinkedResources(ObjectId datasetId, boolean delete) {
        PitagoraDataset dataset = findById(datasetId);

        long numOfRecords = sp.dataRecordsService.countAllByDataset(dataset.getName());
        int numOfAlarms = sp.alarmService.countAllByDatasetId(dataset.getId());
        int numOfAlarmEvents = sp.alarmEventService.countAllByDatasetId(dataset.getId());
        if (delete) {
            sp.jobService.deleteByDatasetId(dataset.getId());
            sp.dataRecordsService.dropCollection(dataset.getName());
            sp.alarmService.deleteAllByDatasetId(dataset.getId());
            sp.alarmEventService.deleteAllByDatasetId(dataset.getId());
            datasetRepository.deleteById(dataset.getId());
            log.info("Dataset " + dataset.getName() + " deleted");
        }
        return ResourceToBeDeletedDTO.buildForDatasetDeletion(dataset.getName(), numOfRecords, numOfAlarms, numOfAlarmEvents);
    }

    public List<DatasetDetailsDTO> getAllDatasetDetails() {
        return _findDatasetWithAggregations().stream()
                .filter(ds -> sp.jwtService.userHasRightsOn(ds))
                .map(ds -> {
                    DatasetDetailsDTO dto = DatasetDetailsDTO.fromModel(ds);
                    dto.setRecords(sp.dataRecordsService.countAllByDataset(ds.getName()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    public PitagoraDataset findDatasetByIdWithAggregations(String datasetId) {
        return _findDatasetWithAggregations(Aggregation.match(Criteria.where("_id").is(new ObjectId(datasetId)))).get(0);
    }
    public PitagoraDataset findDatasetByNameWithAggregations(String datasetName) {
        return _findDatasetWithAggregations(Aggregation.match(
                QueryUtils.buildQueryCriteria(
                        Filter.builder()
                                .field("name")
                                .fieldType(FieldType.STRING)
                                .value(datasetName)
                                .operation(Operation.EQUALS)
                                .build(),"name"))).get(0);
    }

    private List<PitagoraDataset> _findDatasetWithAggregations(AggregationOperation... moreAggregation) {
        List<AggregationOperation> aggregationsOps = new ArrayList<>();
        aggregationsOps.add(Aggregation.lookup("model", "modelId", "_id", "model"));
        aggregationsOps.addAll(Arrays.asList(moreAggregation));
        return sp.mongoTemplate.aggregate(Aggregation.newAggregation(aggregationsOps),
                PitagoraDataset.class, PitagoraDataset.class)
                .getMappedResults();
    }
    public DatasetDetailsDTO getDatasetDetails(ObjectId datasetId) {

        PitagoraDataset ds = findById(datasetId);
        DatasetDetailsDTO datasetDetailsDTO = DatasetDetailsDTO.fromModel(ds);
        datasetDetailsDTO.setRecords(sp.dataRecordsService.countAllByDataset(ds.getName()));
        datasetDetailsDTO.setGenerators(ds.getGenerators().stream().map(gen ->
                DatasetGeneratorDTO.builder()
                        .source(gen.getSourceId() != null ? sp.sourceService.getSourceById(gen.getSourceId()).getNameForClient() : "")
                        .mapper(gen.getMapperId() != null ? sp.mapperService.getMapperById(gen.getMapperId()).getNameForClient() : "")
                        .generatedAt(gen.getGeneratedAt())
                        .build()).collect(Collectors.toList())
        );

        return datasetDetailsDTO;
    }

    public ModelDTO getModelByDatasetId(ObjectId datasetId) {
        PitagoraDataset ds = findById(datasetId);
        return ModelDTO.fromModel(sp.modelService.getModelById(ds.getModelId()));
    }

    public FileDTO exportFile(QueryDatasetDTO query, ExportFormat exportFormat) {

        DatasetTableDataDTO datasetData = sp.dataRecordsService.getAllRecordsByQuery(query);
        List<Map<String, Object>> data = datasetData.getData();
        String datasetName = findById(new ObjectId(query.getDatasetId())).getName();

        List<String> extraDetails = new ArrayList<>();
        extraDetails.add("Dataset: " + datasetName);
        extraDetails.add("Records: " + data.size());
        extraDetails.add(t(EXPORTED_ON) + LocalDateTime.now());

        List<String> headerDescription = new ArrayList<>();
        List<String> headersKeys = new ArrayList<>();

        datasetData.getFields().forEach(field -> {
            headerDescription.add(field.getDescription());
            headersKeys.add(field.getName());
        });

        String contentType = "";
        ByteArrayOutputStream bos;
        if (exportFormat.equals(ExportFormat.FILE_EXCEL)) {
            contentType = EXCEL_CONTENT_TYPE;
            bos = sp.excelService._exportExcel(data, datasetName, headerDescription, headersKeys, extraDetails);
        } else if (exportFormat.equals(ExportFormat.FILE_CSV)) {
            contentType = CSV_CONTENT_TYPE;
            bos = sp.csvService._exportCsv(data, headerDescription, headersKeys, extraDetails);
        } else {
            throw PitagoraException.internalServerError();
        }

        FileDTO file = new FileDTO();
        file.setFileName(datasetName);
        file.setDataBase64("data:" + contentType + ";base64," + EncodingUtils.encodeBase64(bos.toByteArray()));
        file.setContentType(contentType);

        return file;
    }

    public List<PitagoraDataset> findAllDatasetsBySourceId(ObjectId sourceId) {
        return _findAllDatasetByGeneratorProperty("sourceId", sourceId);
    }

    public List<PitagoraDataset> findAllDatasetsByMapperid(ObjectId mapperId) {
        return _findAllDatasetByGeneratorProperty("mapperId", mapperId);
    }

    private List<PitagoraDataset> _findAllDatasetByGeneratorProperty(String property, ObjectId id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("generators").elemMatch(Criteria.where(property).is(id)));
        return sp.mongoTemplate.find(query, PitagoraDataset.class);
    }

    @Transactional
    public void saveAll(List<PitagoraDataset> datasets) {
        datasetRepository.saveAll(datasets);
    }

    @Transactional
    public void deleteAll(List<PitagoraDataset> datasets) {
        datasetRepository.deleteAll(datasets);
    }

    public List<PitagoraDataset> findAllByModelId(ObjectId modelId) {
        return datasetRepository.findAllByModelId(modelId);
    }

    public Optional<PitagoraDataset> findByName(String name) {
        return datasetRepository.findByName(name);
    }

    public PitagoraDataset createAggregatedDataset(String newDatasetName, ObjectId modelId) {
        DatasetCreationDTO aggregatedDataset = DatasetCreationDTO.builder()
                .name(newDatasetName)
                .modelId(modelId.toString())
                .scope(List.of(Theorem.ANALYSIS_TOOLS))
                .build();
        return createDataset(aggregatedDataset);
    }
}
