package it.apeiron.pitagora.core.controller;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.DATASET;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_DELETED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_UPDATED;

import it.apeiron.pitagora.core.dto.DatasetCreationDTO;
import it.apeiron.pitagora.core.dto.ExportFileCallDTO;
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
@RequestMapping("dataset")
public class DatasetController {

    private final ServiceProvider sp;

    @GetMapping
    public ResponseEntity<ResponseDTO> getAllDatasets() {
        return ResponseDTO.ok(sp.datasetService.getAllDatasets());
    }

    @GetMapping("/{datasetId}")
    public ResponseEntity<ResponseDTO> get(@PathVariable("datasetId") ObjectId datasetId) {
        return ResponseDTO.ok(sp.datasetService.getDatasetDetails(datasetId));
    }

    @GetMapping("/details")
    public ResponseEntity<ResponseDTO> getAllDatasetDetails() {
        return ResponseDTO.ok(sp.datasetService.getAllDatasetDetails());
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> createDataset(@RequestBody DatasetCreationDTO dto) {
        sp.datasetService.createDataset(dto);
        return ResponseDTO.created(t(DATASET, SUCCESSFULLY_CREATED));
    }

    @PutMapping
    public ResponseEntity<ResponseDTO> updateModel(@RequestBody DatasetCreationDTO dto) {
        sp.datasetService.update(dto);
        return ResponseDTO.ok(null, t(DATASET, SUCCESSFULLY_UPDATED));
    }

    @DeleteMapping("{datasetId}")
    public ResponseEntity<ResponseDTO> deleteModel(@PathVariable("datasetId") ObjectId datasetId) {
        sp.datasetService.delete(datasetId);
        return ResponseDTO.ok(null, t(DATASET, SUCCESSFULLY_DELETED));
    }

    @GetMapping("linked/{datasetId}")
    public ResponseEntity<ResponseDTO> getLinkedResurces(@PathVariable("datasetId") ObjectId datasetId) {
        return ResponseDTO.ok(sp.datasetService.getLinkedResources(datasetId));
    }

    @GetMapping("model/{datasetId}")
    public ResponseEntity<ResponseDTO> getModelByDatasetId(@PathVariable("datasetId") ObjectId datasetId) {
        return ResponseDTO.ok(sp.datasetService.getModelByDatasetId(datasetId));
    }

    @PostMapping("export")
    public ResponseEntity<ResponseDTO> exportFileExcel(@RequestBody ExportFileCallDTO exportFileCallDTO) {
        return ResponseDTO.ok(sp.datasetService.exportFile(exportFileCallDTO.getQuery(), exportFileCallDTO.getExportFormat()));
    }

}
