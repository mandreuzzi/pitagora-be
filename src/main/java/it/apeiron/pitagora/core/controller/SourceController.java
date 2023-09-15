package it.apeiron.pitagora.core.controller;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.SOURCE;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED_F;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_DELETED_F;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_UPDATED_F;

import it.apeiron.pitagora.core.dto.HttpSourceDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.service.ServiceProvider;
import javax.validation.Valid;
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
@RestController
@RequiredArgsConstructor
@RequestMapping("source")
public class SourceController {

    private final ServiceProvider sp;

    @GetMapping("all")
    public ResponseEntity<ResponseDTO> findAllSources() {
        return ResponseDTO.ok(sp.sourceService.findAll());
    }

    @GetMapping("crud/all/{channel}")
    public ResponseEntity<ResponseDTO> findAllByChannel(@PathVariable("channel") SourceChannel channel) {
        return ResponseDTO.ok(sp.sourceService.findAllSourcesByChannel(channel));
    }

    @GetMapping("crud/{sourceId}")
    public ResponseEntity<ResponseDTO> get(@PathVariable("sourceId") ObjectId sourceId) {
        return ResponseDTO.ok(sp.sourceService.getSourceConfigById(sourceId));
    }

    @PostMapping("crud/{channel}")
    public ResponseEntity<ResponseDTO> create(@PathVariable("channel") String channel, @Valid @RequestBody String json) {
        sp.sourceService.create(json, channel);
        return ResponseDTO.created(t(SOURCE, SUCCESSFULLY_CREATED_F));
    }

    @PutMapping("crud/{channel}")
    public ResponseEntity<ResponseDTO> update(@PathVariable("channel") String channel, @Valid @RequestBody String json) {
        sp.sourceService.update(json, channel);
        return ResponseDTO.ok(null, t(SOURCE, SUCCESSFULLY_UPDATED_F));
    }

    @DeleteMapping("crud/{id}")
    public ResponseEntity<ResponseDTO> delete(@PathVariable("id") ObjectId id) {
        sp.sourceService.delete(id);
        return ResponseDTO.ok(null, t(SOURCE, SUCCESSFULLY_DELETED_F));
    }

    @GetMapping("downloadFile/{id}")
    public ResponseEntity<ResponseDTO> download(@PathVariable("id") ObjectId id) {
        return ResponseDTO.ok(sp.sourceService.download(id));
    }

    @GetMapping("previewSource/{id}")
    public ResponseEntity<ResponseDTO> preview(@PathVariable("id") ObjectId id) {
        return ResponseDTO.ok(sp.sourceService.preview(id));
    }

    @PostMapping("testApi")
    public ResponseEntity<ResponseDTO> testApi(@RequestBody HttpSourceDTO dto) {
        return ResponseDTO.ok(sp.httpService.getHttpSourceResponseNow(dto));
    }

    @GetMapping("linked/{id}")
    public ResponseEntity<ResponseDTO> getLinkedResurces(@PathVariable("id") ObjectId id) {
        return ResponseDTO.ok(sp.sourceService.getLinkedResurces(id));
    }

    @GetMapping("apiKey")
    public ResponseEntity<ResponseDTO> requestKeyForExposedApiConfiguration() {
        return ResponseDTO.ok(sp.sourceService.requestKeyForExposedApiConfiguration());
    }
}
