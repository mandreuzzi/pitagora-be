package it.apeiron.pitagora.core.controller;


import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraScopeCoordinates;
import it.apeiron.pitagora.core.service.ClassificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("classification")
public class ClassificationController {

    private final ClassificationService service;

    @GetMapping
    public ResponseEntity<ResponseDTO> getAll() {
        return ResponseDTO.ok(service.getAllScope());
    }

    @GetMapping("/{value}")
    public ResponseEntity<ResponseDTO> getDocumentByValueDescription(@PathVariable("value") String scope) {
        return ResponseDTO.ok(service.getDocumentByArea(scope));
    }

    @PostMapping("")
    public ResponseEntity<ResponseDTO> insertNewScope(@RequestBody PitagoraScopeCoordinates scopeCoordinates) {
        return ResponseDTO.ok(service.insertNewScope(scopeCoordinates));
    }

    @PostMapping("/category")
    public ResponseEntity<ResponseDTO> insertNewCategory(@RequestBody PitagoraScopeCoordinates scopeCoordinates) {
        return ResponseDTO.ok(service.insertNewCategory(scopeCoordinates));
    }

    @PostMapping("/subCategory")
    public ResponseEntity<ResponseDTO> insertNewSubCategory(@RequestBody PitagoraScopeCoordinates scopeCoordinates) {
        return ResponseDTO.ok(service.insertNewSubCategory(scopeCoordinates));
    }

    @PutMapping("")
    public ResponseEntity<ResponseDTO> editScope(@RequestBody List<ValueDescriptionDTO> valueDescriptionDTOS) {
        return ResponseDTO.ok(service.editScope(valueDescriptionDTOS));
    }

    @PutMapping("/category")
    public ResponseEntity<ResponseDTO> editCategory(@RequestBody PitagoraScopeCoordinates scopeCoordinates) {
        return ResponseDTO.ok(service.editCategory(scopeCoordinates));
    }

    @PutMapping("/subCategory")
    public ResponseEntity<ResponseDTO> editSubCategory(@RequestBody PitagoraScopeCoordinates scopeCoordinates) {
        return ResponseDTO.ok(service.editSubCategory(scopeCoordinates));
    }

}
