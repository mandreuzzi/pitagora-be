package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.entity.enums.SourceChannel.HTTP;
import static it.apeiron.pitagora.core.util.Language.t;

import it.apeiron.pitagora.core.dto.MapperDTO;
import it.apeiron.pitagora.core.dto.ResourceToBeDeletedDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.FileExcelExtraConfig;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraMapperRepository;
import it.apeiron.pitagora.core.util.MessagesCore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@RequiredArgsConstructor
@Service
public class MapperService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraMapperRepository mapperRepository;

    public PitagoraMapper getMapperById(ObjectId mapperId) {
        return mapperRepository.findById(mapperId)
                .orElseThrow(() -> PitagoraException.notAcceptable("Mapper with id '" + mapperId + "' not found"));
    }

    public List<ValueDescriptionDTO> findAll() {
        return mapperRepository.findAll().stream()
                .filter(mapper -> {
                    if (mapper.getReferenceSourceId() == null) {
                        return false;
                    }
                    PitagoraModel model = sp.modelService.getModelById(mapper.getReferenceModelId());
                    return sp.jwtService.userHasRightsOn(model);
                })
                .map(ValueDescriptionDTO::fromResource).collect(Collectors.toList());
    }


    @Transactional
    public void create(MapperDTO dto) {
        if (mapperRepository.existsByName(dto.getName())) {
            throw PitagoraException.nameNotAvailable();
        }
        PitagoraSource source = sp.sourceService.getSourceById(new ObjectId(dto.getReferenceSourceId()));
        _getFileExcelExtraConfig(source, dto);

        PitagoraMapper toBeCreated = new PitagoraMapper(dto, source.getChannel());
        _checkReferencedModel(toBeCreated);
        mapperRepository.save(toBeCreated);
        log.info("Mapper " + dto.getName() + " created");
    }

    @Transactional
    public void update(MapperDTO dto) {
        PitagoraMapper mapper = getMapperById(new ObjectId(dto.getId()));
        if (mapperRepository.existsByName(dto.getName()) && !dto.getName().equals(mapper.getName())) {
            throw PitagoraException.nameNotAvailable();
        }
        PitagoraSource source = sp.sourceService.getSourceById(new ObjectId(dto.getReferenceSourceId()));
        _getFileExcelExtraConfig(source, dto);

        mapper.update(dto, source.getChannel());
        _checkReferencedModel(mapper);
        if (HTTP.equals(source.getChannel())) {
            sp.jobService.rescheduleJobsDueToMapperChange(mapper.getId());
        }

        mapperRepository.save(mapper);
        log.info("Mapper " + mapper.getName() + " updated");
    }

    private void _getFileExcelExtraConfig(PitagoraSource source, MapperDTO dto) {
        if (SourceChannel.FILE_EXCEL.equals(source.getChannel()) || SourceChannel.FILE_CSV.equals(source.getChannel())) {
            dto.setExtraConfig(FileExcelExtraConfig.fromSourceConfiguration(source.getFileUploadConfiguration()));
        }
    }

    private void _checkReferencedModel(PitagoraMapper mapper) {
        PitagoraModel model = sp.modelService.getModelById(mapper.getReferenceModelId());
        if (!model.getStructure().keySet().containsAll(mapper.getRules().keySet())) {
            log.error("One or more Rules relate to fields not present in the Model");
            throw PitagoraException.notAcceptable(t(MessagesCore.RULE_ON_MISSING_MODEL_FIELD));
        }
        PitagoraSource referenceSource = sp.sourceService.getSourceById(mapper.getReferenceSourceId());
        String data;
        switch (referenceSource.getChannel()) {
            case HTTP:
                data = sp.sourceService.getStoredHttpSourceData(referenceSource.getId());
                sp.jsonService.checkTypeMatching(mapper, data);
                return;
            case FILE_EXCEL:
                data = sp.sourceService.getFileById((referenceSource.getFileUploadConfiguration()).getFileId()).getDataBase64();
                sp.excelService.checkTypeMatching(mapper, data);
                return;
            case FILE_CSV:
                data = sp.sourceService.getFileById((referenceSource.getFileUploadConfiguration()).getFileId()).getDataBase64();
                sp.csvService.checkTypeMatching(mapper, data);
                return;
            case EXPOSED_API:
                log.error("Source EXPOSED_API does not need a Mapper");
                throw PitagoraException.notAcceptable(t(MessagesCore.CHANNEL_NOT_NEED_MAPPER));
            default:
                log.error("Unknown Source Channel");
                throw PitagoraException.notAcceptable("Unknown Source Channel");
        }

    }

    @Transactional
    public void delete(ObjectId mapperId) {
        handleLinkedResources(mapperId, true);
        mapperRepository.deleteById(mapperId);
        sp.jobService.deleteByMapperId(mapperId);
    }

    public List<PitagoraMapper> findAllByReferenceSourceId(ObjectId sourceId) {
        return mapperRepository.findAllByReferenceSourceId(sourceId);
    }

    public List<ValueDescriptionDTO> getAvailableMappersBySourceAndModel(ObjectId sourceId, ObjectId modelId) {
        return mapperRepository.findAllByReferenceSourceIdAndReferenceModelId(sourceId, modelId).stream()
                .map(ValueDescriptionDTO::fromResource).collect(Collectors.toList());
    }

    public ResourceToBeDeletedDTO getLinkedResources(ObjectId mapperId) {
        return handleLinkedResources(mapperId, false);
    }

    @Transactional
    public ResourceToBeDeletedDTO handleLinkedResources(ObjectId mapperId, boolean delete) {
        PitagoraMapper mapper = getMapperById(mapperId);
        List<PitagoraDataset> linkedDataset = sp.datasetService.findAllDatasetsByMapperid(mapper.getId());
        if (delete) {
            sp.datasetService.saveAll(linkedDataset.stream().peek(ds ->
                            ds.getGenerators().forEach(gen -> {
                                if (mapper.getId().equals(gen.getMapperId())) {
                                    gen.setMapperId(null);
                                }
                            })
                    ).collect(Collectors.toList())
            );
            log.info("Mapper " + mapper.getName() + " deleted");
        }
        return ResourceToBeDeletedDTO.build(mapper, linkedDataset, false, Collections.emptyList());
    }

    @Transactional
    public void deleteAll(List<PitagoraMapper> mappers) {
        mapperRepository.deleteAll(mappers);
    }

    public List<PitagoraMapper> findAllByReferenceModelId(ObjectId modelId) {
        return mapperRepository.findAllByReferenceModelId(modelId);
    }

    @Transactional
    public void updateFileExtraConfig(PitagoraSource src) {
        findAllByReferenceSourceId(src.getId()).forEach(mapper -> {
            mapper.setExtraConfig(FileExcelExtraConfig.fromSourceConfiguration(src.getFileUploadConfiguration()));
            mapperRepository.save(mapper);
        });
    }

    public Optional<PitagoraMapper> findByName(String name) {
        return mapperRepository.findByName(name);
    }
}
