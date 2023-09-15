package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;

import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO.AggregatedModelDTO;
import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO.AggregatedModelFieldDTO;
import it.apeiron.pitagora.core.dto.ModelDTO;
import it.apeiron.pitagora.core.dto.ResourceToBeDeletedDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraModelRepository;
import it.apeiron.pitagora.core.util.MessagesCore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ModelService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraModelRepository modelRepository;

    public List<ValueDescriptionDTO> findAll() {
        return modelRepository.findAll().stream()
                .filter(model -> sp.jwtService.userHasRightsOn(model))
                .map(ValueDescriptionDTO::fromResource).collect(Collectors.toList());
    }

    public PitagoraModel getModelById(ObjectId modelId) {
        return modelRepository.findById(modelId)
                .orElseThrow(() -> PitagoraException.notAcceptable("Model with id '" + modelId + "' not found"));
    }

    @Transactional
    public PitagoraModel create(ModelDTO dto) {
        _checkModelNameForCreation(dto.getName());
        PitagoraModel saved = modelRepository.save(new PitagoraModel(dto));
        log.info("Model " + dto.getName() + " created");
        return saved;
    }

    @Transactional
    public void update(ModelDTO dto) {
        PitagoraModel model = getModelById(new ObjectId(dto.getId()));
        if (modelRepository.existsByName(dto.getName()) && !dto.getName().equals(model.getName())) {
            throw PitagoraException.nameNotAvailable();
        }

        if (!handleLinkedResources(model.getId(), false).getToBeDeleted().isEmpty()) {
            model.getStructure().forEach((key, value) -> {
                List<ModelField> unique = dto.getStructure().stream().filter(attr -> attr.getName().equals(key))
                        .collect(Collectors.toList());
                if (unique.size() != 1 || !unique.get(0).getType().equals(value.getType())) {
                    throw PitagoraException.notAcceptable(t(MessagesCore.MODEL_CHANGES_NOT_ALLOWED));
                }
            });
        }

        model.update(dto);
        modelRepository.save(model);
        log.info("Model " + dto.getName() + " updated");
    }

    @Transactional
    public void delete(ObjectId modelId) {
        handleLinkedResources(modelId, true);
        modelRepository.deleteById(modelId);
    }

    public ResourceToBeDeletedDTO getLinkedResurces(ObjectId modelId) {
        return handleLinkedResources(modelId, false);
    }

    @Transactional
    public ResourceToBeDeletedDTO handleLinkedResources(ObjectId modelId, boolean delete) {
        PitagoraModel model = getModelById(modelId);

        List<PitagoraMapper> linkedMappers = sp.mapperService.findAllByReferenceModelId(modelId);
        List<PitagoraDataset> linkedDataset = sp.datasetService.findAllByModelId(modelId);
        if (delete) {
            sp.mapperService.deleteAll(linkedMappers);
            sp.datasetService.deleteAll(linkedDataset);
            log.info("Model " + model.getName() + " deleted");
        }
        return ResourceToBeDeletedDTO.build(model, linkedDataset, true, linkedMappers);
    }

    public PitagoraModel getDatasetModel(ObjectId datasetId) {
        PitagoraDataset ds = sp.datasetService.findById(datasetId);
        return getModelById(ds.getModelId());
    }

    public Optional<PitagoraModel> findByName(String name) {
        return modelRepository.findByName(name);
    }

    public PitagoraModel createAggregatedModel(String name, AggregatedModelDTO aggregatedModel) {
        _checkModelNameForCreation(name);
        _checkModelStructure(aggregatedModel);

        LinkedList<ModelField> structure = new LinkedList<>();
        aggregatedModel.getFirstDataset().forEach(aggrField -> structure.add(aggrField.getTo()));
        aggregatedModel.getSecondDataset().forEach(aggrField -> structure.add(aggrField.getTo()));
        ModelDTO model = ModelDTO.builder()
                .name(name)
                .structure(structure)
                .scope(List.of(Theorem.ANALYSIS_TOOLS))
                .build();
        return create(model);
    }

    private void _checkModelStructure(AggregatedModelDTO aggregatedModel) {
        Set<String> keys = new HashSet<>();

        aggregatedModel.getFirstDataset().forEach(aggrModelField -> _checkField(keys, aggrModelField));
        aggregatedModel.getSecondDataset().forEach(aggrModelField -> _checkField(keys, aggrModelField));

        if (keys.size() != aggregatedModel.getFirstDataset().size() + aggregatedModel.getSecondDataset().size()) {
            throw PitagoraException.badRequest("One or more fields are missing in the aggregated model");
        }
    }

    private void _checkField(Set<String> keys, AggregatedModelFieldDTO aggrModelField) {
        if (!aggrModelField.getFrom().getType().equals(aggrModelField.getTo().getType())) {
            throw PitagoraException.badRequest("Originating field '" + aggrModelField.getFrom().getName() + " and aggregated field '" + aggrModelField
                    .getTo().getName() + "' type mismatch" );
        }
        keys.add(aggrModelField.getTo().getName());
    }

    private void _checkModelNameForCreation(String name) {
        if (modelRepository.existsByName(name)) {
            throw PitagoraException.nameNotAvailable();
        }
    }

    public static AggregatedModelDTO buildDefaultAggregateModelDTO(PitagoraModel toBeEnriched, PitagoraModel enriching,
            String prefixOfNewAttributes) {
        AggregatedModelDTO aggregateModel = new AggregatedModelDTO();
        LinkedHashMap<String, ModelField> toBeEnrichedStructure = toBeEnriched.getStructure();
        aggregateModel.setFirstDataset(
                toBeEnrichedStructure.values().stream()
                        .map(AggregatedModelFieldDTO::new).collect(Collectors.toList())
        );
        List<String> forbidden = toBeEnrichedStructure.values().stream().map(ModelField::getName).collect(Collectors.toList());
        aggregateModel.setSecondDataset(_buildEnrichingPart(enriching.getStructure(), prefixOfNewAttributes, forbidden));
        return aggregateModel;
    }

    public static List<AggregatedModelFieldDTO> _buildEnrichingPart(LinkedHashMap<String, ModelField> fromStructure, String prefix, List<String> forbidden) {
        List<AggregatedModelFieldDTO> aggregateModel = new ArrayList<>();
        for (ModelField field : fromStructure.values()) {
            AggregatedModelFieldDTO aggregField = new AggregatedModelFieldDTO();
            aggregField.setFrom(field);
            String name = _safeName(prefix, field.getName(), forbidden);
            aggregField.setTo(ModelField.builder()
                    .name(name)
                    .description(prefix + field.getDescription())
                    .type(field.getType())
                    .build());
            aggregateModel.add(aggregField);
            forbidden.add(name);
        }

        return aggregateModel;
    }

    public static LinkedHashMap<String, ModelField> buildDefaultEnrichedStructure(LinkedHashMap<String, ModelField> toBeEnriched, LinkedHashMap<String, ModelField> enriching,
            String prefixOfNewAttributes) {
        LinkedHashMap<String, ModelField> result = new LinkedHashMap<>(toBeEnriched);
        List<String> forbidden = toBeEnriched.values().stream().map(ModelField::getName).collect(Collectors.toList());
        for (ModelField field : enriching.values()) {
            String name = _safeName(prefixOfNewAttributes, field.getName(), forbidden);
            result.put(name, ModelField.builder()
                    .name(name)
                    .description(prefixOfNewAttributes + field.getDescription())
                    .type(field.getType())
                    .build());
            forbidden.add(name);
        }
        return result;
    }

    private static String _safeName(String prefix, String name, List<String> forbidden) {
        String safe = prefix + name;
        if (forbidden.contains(safe)) {
            return _safeName(prefix, safe, forbidden);
        }
        return safe;
    }

}
