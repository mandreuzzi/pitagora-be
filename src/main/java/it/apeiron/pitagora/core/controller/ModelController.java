package it.apeiron.pitagora.core.controller;


import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.MODEL;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_DELETED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_UPDATED;

import it.apeiron.pitagora.core.dto.ModelDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("model")
public class ModelController {

    private final ServiceProvider sp;

    @GetMapping
    public ResponseEntity<ResponseDTO> getAll() {
        return ResponseDTO.ok(sp.modelService.findAll());
    }

    @GetMapping("/{modelId}")
    public ResponseEntity<ResponseDTO> get(@PathVariable("modelId") ObjectId modelId) {
        return ResponseDTO.ok(ModelDTO.fromModel(sp.modelService.getModelById(modelId)));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createModel(@RequestBody ModelDTO dto) {
        sp.modelService.create(dto);
        return ResponseDTO.created(t(MODEL, SUCCESSFULLY_CREATED));
    }

    @PutMapping
    public ResponseEntity<ResponseDTO> updateModel(@RequestBody ModelDTO dto) {
        sp.modelService.update(dto);
        return ResponseDTO.ok(null, t(MODEL, SUCCESSFULLY_UPDATED));
    }

    @DeleteMapping("{modelId}")
    public ResponseEntity<ResponseDTO> deleteModel(@PathVariable("modelId") ObjectId modelId) {
        sp.modelService.delete(modelId);
        return ResponseDTO.ok(null, t(MODEL, SUCCESSFULLY_DELETED));
    }

    @GetMapping("linked/{modelId}")
    public ResponseEntity<ResponseDTO> getLinkedResurces(@PathVariable("modelId") ObjectId modelId) {
        return ResponseDTO.ok(sp.modelService.getLinkedResurces(modelId));
    }

}
