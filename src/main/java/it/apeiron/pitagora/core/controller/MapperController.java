package it.apeiron.pitagora.core.controller;


import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.MAPPER;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_DELETED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_UPDATED;

import it.apeiron.pitagora.core.dto.MapperDTO;
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
@RequestMapping("mapper")
public class MapperController {

    private final ServiceProvider sp;

    @GetMapping
    public ResponseEntity<ResponseDTO> findAll() {
        return ResponseDTO.ok(sp.mapperService.findAll());
    }

    @GetMapping("{mapperId}")
    public ResponseEntity<ResponseDTO> getMapper(@PathVariable("mapperId") ObjectId mapperId) {
        return ResponseDTO.ok(MapperDTO.fromModel(sp.mapperService.getMapperById(mapperId)));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createMapper(@RequestBody MapperDTO dto) {
        sp.mapperService.create(dto);
        return ResponseDTO.created(t(MAPPER, SUCCESSFULLY_CREATED));
    }

    @PutMapping
    public ResponseEntity<ResponseDTO> updateMapper(@RequestBody MapperDTO dto) {
        sp.mapperService.update(dto);
        return ResponseDTO.ok(null, t(MAPPER, SUCCESSFULLY_UPDATED));
    }

    @DeleteMapping("{mapperId}")
    public ResponseEntity<ResponseDTO> deleteModel(@PathVariable("mapperId") ObjectId mapperId) {
        sp.mapperService.delete(mapperId);
        return ResponseDTO.ok(null, t(MAPPER, SUCCESSFULLY_DELETED));
    }

    @GetMapping("linked/{mapperId}")
    public ResponseEntity<ResponseDTO> getLinkedResources(@PathVariable("mapperId") ObjectId mapperId) {
        return ResponseDTO.ok(sp.mapperService.getLinkedResources(mapperId));
    }

    @GetMapping("available/{sourceId}/{modelId}")
    public ResponseEntity<ResponseDTO> getAvailableMappers(@PathVariable("sourceId") ObjectId sourceId,
            @PathVariable("modelId") ObjectId modelId) {
        return ResponseDTO.ok(sp.mapperService.getAvailableMappersBySourceAndModel(sourceId, modelId));
    }
}
