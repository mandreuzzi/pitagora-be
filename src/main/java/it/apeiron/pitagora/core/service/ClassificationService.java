package it.apeiron.pitagora.core.service;

import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraScopeCoordinates;
import it.apeiron.pitagora.core.repository.DocumentsScopeCoordinatesRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ClassificationService {

    private final DocumentsScopeCoordinatesRepo coordinatesRepo;

    public PitagoraScopeCoordinates insertNewScope(PitagoraScopeCoordinates scopeCoordinates) {
        return coordinatesRepo.save(scopeCoordinates);
    }

    public PitagoraScopeCoordinates insertNewCategory(PitagoraScopeCoordinates scopeCoordinates) {
        PitagoraScopeCoordinates pitagoraScopeCoordinates = this.getDocumentByArea(scopeCoordinates.getValueDescription().getValue());
        pitagoraScopeCoordinates.setCategories(scopeCoordinates.getCategories());
        return coordinatesRepo.save(pitagoraScopeCoordinates);
    }

    public PitagoraScopeCoordinates insertNewSubCategory(PitagoraScopeCoordinates scopeCoordinates) {
        PitagoraScopeCoordinates tree = this.getDocumentByArea(scopeCoordinates.getValueDescription().getValue());
        tree.setCategories(scopeCoordinates.getCategories());
        return coordinatesRepo.save(tree);
    }

    public List<PitagoraScopeCoordinates> getAllScope() {
        return coordinatesRepo.findAll();
    }

    public PitagoraScopeCoordinates getDocumentByArea(String value) {
        return coordinatesRepo.findByValueDescription_Value(value).orElse(null);
    }

    public PitagoraScopeCoordinates editScope(List<ValueDescriptionDTO> valueDescriptionDTOS) {
        PitagoraScopeCoordinates pitagoraScopeCoordinates = getDocumentByArea(valueDescriptionDTOS.get(0).getValue());
        pitagoraScopeCoordinates.setValueDescription(valueDescriptionDTOS.get(1));
        return coordinatesRepo.save(pitagoraScopeCoordinates);
    }

    public PitagoraScopeCoordinates editCategory(PitagoraScopeCoordinates scopeCoordinates) {

        PitagoraScopeCoordinates pitagoraScopeCoordinates = getDocumentByArea(scopeCoordinates.getValueDescription().getValue());
        pitagoraScopeCoordinates.setCategories(scopeCoordinates.getCategories());

        return coordinatesRepo.save(pitagoraScopeCoordinates);
    }

    public PitagoraScopeCoordinates editSubCategory(PitagoraScopeCoordinates scopeCoordinates) {

        PitagoraScopeCoordinates pitagoraScopeCoordinates = getDocumentByArea(scopeCoordinates.getValueDescription().getValue());
        pitagoraScopeCoordinates.setCategories(scopeCoordinates.getCategories());

        return coordinatesRepo.save(pitagoraScopeCoordinates);
    }
}
