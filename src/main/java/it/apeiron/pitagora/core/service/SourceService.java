package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.entity.enums.SourceChannel.EXPOSED_API;
import static it.apeiron.pitagora.core.entity.enums.SourceChannel.FILE_CSV;
import static it.apeiron.pitagora.core.entity.enums.SourceChannel.FILE_EXCEL;
import static it.apeiron.pitagora.core.entity.enums.SourceChannel.FILE_MP4;
import static it.apeiron.pitagora.core.entity.enums.SourceChannel.HTTP;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.dto.ExposedApiSourceDTO;
import it.apeiron.pitagora.core.dto.FileDTO;
import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.dto.HttpSourceDTO;
import it.apeiron.pitagora.core.dto.ResourceToBeDeletedDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraExposedApiKey;
import it.apeiron.pitagora.core.entity.collection.PitagoraFile;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.entity.enums.UpdateRateMode;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraExposedApiKeyRepository;
import it.apeiron.pitagora.core.repository.PitagoraFileRepository;
import it.apeiron.pitagora.core.repository.PitagoraSourceRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@RequiredArgsConstructor
@Service
public class SourceService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraSourceRepository sourceRepository;
    private final PitagoraFileRepository fileRepository;
    private final PitagoraExposedApiKeyRepository exposedApiKeyRepository;

    private final ObjectMapper om = new ObjectMapper();
    private final PasswordEncoder passwordEncoder;

    public List<ValueDescriptionDTO> findAllSourcesByChannel(SourceChannel channel) {
        List<Theorem> userScopes = sp.jwtService.getLoggedUser().getTheorems();
        return sourceRepository.findAllByChannelOrderByCreatedAtDesc(channel).stream()
                .filter(src ->
                {
                    Set<Theorem> scopesLinkedToSource = new HashSet<>();
                    sp.mapperService.findAllByReferenceSourceId(src.getId()).forEach(ds -> scopesLinkedToSource.addAll(ds.getScope()));
                    sp.datasetService.findAllDatasetsBySourceId(src.getId()).forEach(ds -> scopesLinkedToSource.addAll(ds.getScope()));
                    return userScopes.containsAll(scopesLinkedToSource);
                })
                .map(ValueDescriptionDTO::fromResource)
                .collect(Collectors.toList());
    }

    public List<ValueDescriptionDTO> findAll() {
        List<ValueDescriptionDTO> all = new ArrayList<>();
        all.add(ValueDescriptionDTO.builder().build());
        Arrays.asList(HTTP, FILE_EXCEL, FILE_CSV, EXPOSED_API).forEach(ch -> {
            all.add(ValueDescriptionDTO.builder().description(ch.getDescription()).disabled(true).build());
            all.addAll(findAllSourcesByChannel(ch));
        });
        return all;
    }

    public AbstractRecordDTO getSourceConfigById(ObjectId sourceId) {
        PitagoraSource src = getSourceById(sourceId);
        switch (src.getChannel()) {
            case HTTP:
                return HttpSourceDTO.fromModel(src);
            case FILE_EXCEL:
            case FILE_CSV:
                FileUploadSourceDTO fileDto = FileUploadSourceDTO.fromModel(src);
                PitagoraFile fileById = getFileById(src.getFileUploadConfiguration().getFileId());
                fileDto.setFileName(fileById.getFileName());
                fileDto.setContentType(fileById.getContentType());
                return fileDto;
            case EXPOSED_API:
                return ExposedApiSourceDTO.fromModel(src);
        }
        return null;
    }

    @Transactional
    public void create(String json, String channel) {
        SourceChannel ch = SourceChannel.valueOf(channel.toUpperCase());
        AbstractRecordDTO dto = _parseDto(json, ch);
        if (checkSourceNameExistence(ch, dto.getName())) {
            throw PitagoraException.nameNotAvailable();
        }
        PitagoraSource src = new PitagoraSource(dto, ch, EXPOSED_API.equals(ch) ? exposedApiKeyRepository : fileRepository, sp.bigFileService);
        src.setChannel(ch);
        if (HTTP.equals(ch) || FILE_EXCEL.equals(ch) || EXPOSED_API.equals(ch)) {
            sourceRepository.save(src);
            if (HTTP.equals(ch)) {
                getSampleResponseBody(src);
            }
        } else if (FILE_CSV.equals(ch)) {
            sp.csvService.checkBoundaries(src);
            sourceRepository.save(src);
        } else if (FILE_MP4.equals(ch)) {
            sourceRepository.save(src);
        }

        log.info("Source " + src.getName() + " created");
    }

    @Transactional
    public void update(String json, String channel) {
        SourceChannel ch = SourceChannel.valueOf(channel.toUpperCase());
        AbstractRecordDTO dto = _parseDto(json, ch);
        PitagoraSource src = getSourceById(new ObjectId(dto.getId()));
        if (checkSourceNameExistence(ch, dto.getName()) && !dto.getName().equals(src.getName())) {
            throw PitagoraException.nameNotAvailable();
        }
        src.update(dto, ch, fileRepository, sp.bigFileService);

        if (HTTP.equals(ch) || FILE_EXCEL.equals(ch) || EXPOSED_API.equals(ch)) {
            sourceRepository.save(src);
            if (HTTP.equals(ch)) {
                getSampleResponseBody(src);
                if (UpdateRateMode.PERIODIC.equals(src.getHttpConfiguration().getUpdateRateMode())) {
                    sp.jobService.rescheduleJobsDueToSourceChange(src.getId());
                } else {
                    sp.jobService.deleteBySourceId(src.getId());
                }
            } else if (FILE_EXCEL.equals(ch)) {
                sp.mapperService.updateFileExtraConfig(src);
            }
        } else if (FILE_CSV.equals(ch)) {
            sp.csvService.checkBoundaries(src);
            sourceRepository.save(src);
            sp.mapperService.updateFileExtraConfig(src);
        } else if (FILE_MP4.equals(ch)) {
            sourceRepository.save(src);
        }



        log.info("Source " + src.getName() + " updated");
    }

    @Transactional
    public void getSampleResponseBody(PitagoraSource src) {
        HttpResponseDTO res = sp.httpService.getHttpSourceResponseNow(src.getHttpConfiguration());
        ObjectId savedId = sp.httpService.saveResponse(res, src.getId());
        src.getHttpConfiguration().setSampleResponseId(savedId);
        sourceRepository.save(src);
    }

    @SneakyThrows
    private AbstractRecordDTO _parseDto(String json, SourceChannel channel) {
        if (HTTP.equals(channel)) {
            return om.readValue(json, HttpSourceDTO.class);
        } else if (FILE_EXCEL.equals(channel) || FILE_CSV.equals(channel) || FILE_MP4.equals(channel)) {
            return om.readValue(json, FileUploadSourceDTO.class);
        } else if (EXPOSED_API.equals(channel)) {
            return om.readValue(json, ExposedApiSourceDTO.class);
        }
        log.error("Channel unknown");
        throw PitagoraException.internalServerError();
    }

    public boolean checkSourceNameExistence(SourceChannel channel, String configName) {
        return sourceRepository.existsByChannelAndName(channel, configName);
    }

    public FileDTO download(ObjectId id) {
        PitagoraSource fileSource = getSourceById(id);
        PitagoraFile file = getFileById(fileSource.getFileUploadConfiguration().getFileId());
        return FileDTO.fromModel(file);
    }

    public Object preview(ObjectId id) {
        PitagoraSource source = getSourceById(id);

        switch (source.getChannel()) {
            case HTTP:
                return sp.httpService.buildPreview(source);
            case FILE_EXCEL:
                return sp.excelService.buildPreview(source);
            case FILE_CSV:
                return sp.csvService.buildPreview(source);
            default:
                return null;
        }
    }

    public String getStoredHttpSourceData(ObjectId sourceId) {
        return sp.httpService.buildPreview(getSourceById(sourceId)).getResponseBody();
    }

    public PitagoraSource getSourceById(ObjectId sourceId) {
        return sourceRepository.findById(sourceId)
                .orElseThrow(() -> PitagoraException.notAcceptable("Source with id '" + sourceId + "' not found"));
    }

    protected PitagoraFile getFileById(ObjectId fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> PitagoraException.notAcceptable("File with id '" + fileId + "' not found"));
    }

    @Transactional
    public void delete(ObjectId sourceId) {
        handleLinkedResources(sourceId, true);
        PitagoraSource source = getSourceById(sourceId);
        switch (source.getChannel()) {
            case HTTP:
                sp.jobService.deleteBySourceId(sourceId);
                break;
            case FILE_EXCEL:
            case FILE_CSV:
            case FILE_MP4:
                if (_fileIsUnused(source)) {
                    if (FILE_MP4.equals(source.getChannel())) {
                        sp.bigFileService.deleteById(source.getFileUploadConfiguration().getFileId());
                    } else {
                        fileRepository.deleteById(source.getFileUploadConfiguration().getFileId());
                    }
                }
                break;
            case EXPOSED_API:
                exposedApiKeyRepository.deleteById(source.getExposedApiConfiguration().getKeyId());
                break;
            default:
                log.error("Unknown source channel");
                throw PitagoraException.badRequest("Unknown source channel");
        }

        sourceRepository.deleteById(sourceId);
    }

    public ResourceToBeDeletedDTO getLinkedResurces(ObjectId sourceId) {
        return handleLinkedResources(sourceId, false);
    }

    @Transactional
    public ResourceToBeDeletedDTO handleLinkedResources(ObjectId sourceId, boolean delete) {
        PitagoraSource source = getSourceById(sourceId);
        List<PitagoraMapper> linkedMappers = sp.mapperService.findAllByReferenceSourceId(sourceId);
        List<PitagoraDataset> linkedDataset = sp.datasetService.findAllDatasetsBySourceId(sourceId);
        linkedMappers.forEach(mapper -> linkedDataset.addAll(sp.datasetService.findAllDatasetsByMapperid(mapper.getId())));
        if (delete) {
            sp.mapperService.deleteAll(linkedMappers);
            List<ObjectId> mapperIds = linkedMappers.stream().map(PitagoraMapper::getId).collect(Collectors.toList());
            mapperIds.forEach(sp.jobService::deleteByMapperId);
            sp.datasetService.saveAll(linkedDataset.stream().peek(ds ->
                    ds.getGenerators().forEach(gen -> {
                        if (mapperIds.contains(gen.getMapperId())) {
                            gen.setMapperId(null);
                        }
                        if (sourceId.equals(gen.getSourceId())) {
                            gen.setSourceId(null);
                        }
                    })
            ).collect(Collectors.toList()));
            log.info("Source " + source.getName() + " deleted");
        }
        return ResourceToBeDeletedDTO.build(source, linkedDataset, false, linkedMappers);
    }

    private boolean _fileIsUnused(PitagoraSource source) {
        Query query = new Query();
        query.addCriteria(Criteria.where("configuration.fileId").is(source.getFileUploadConfiguration().getFileId()));
        query.addCriteria(Criteria.where("id").ne(source.getId()));
        List<PitagoraSource> pitagoraSources = sp.mongoTemplate.find(query, PitagoraSource.class);
        return pitagoraSources.isEmpty();
    }

    @Transactional
    public ValueDescriptionDTO requestKeyForExposedApiConfiguration() {
        String key = UUID.randomUUID().toString().substring(0, 16);
        PitagoraExposedApiKey saved = exposedApiKeyRepository.save(PitagoraExposedApiKey.builder()
                .enabled(false)
                .hashkey(passwordEncoder.encode(key))
                .build());
        return new ValueDescriptionDTO(saved.getId(), key);
    }

    public PitagoraSource getSourceByApiKey(String apiKey) {
        AtomicReference<PitagoraExposedApiKey> key = new AtomicReference<>();
        for (PitagoraExposedApiKey k : exposedApiKeyRepository.findAll()) {
            if (passwordEncoder.matches(apiKey, k.getHashkey())) {
                key.set(k);
                break;
            }
        }
        if (key.get() == null || !key.get().isEnabled()) {
            throw PitagoraException.forbidden("API Key not valid");
        }
        Query q = new Query().addCriteria(Criteria.where("configuration.keyId").is(key.get().getId()));
        List<PitagoraSource> found = sp.mongoTemplate.find(q, PitagoraSource.class);
        if (found.size() != 1) {
            throw PitagoraException.forbidden("API Key not valid");
        }

        return found.get(0);
    }

    public PitagoraSource findByName(String name) {
        return sourceRepository.findByName(name)
                .orElseThrow(() -> PitagoraException.notAcceptable("Source with name '" + name + "' not found"));
    }
}
