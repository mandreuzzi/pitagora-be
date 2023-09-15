package it.apeiron.pitagora.core.controller;


import static it.apeiron.pitagora.core.util.Language.t;

import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO;
import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.dto.SelectedRecordsToBeDeleted;
import it.apeiron.pitagora.core.dto.SingleValueDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.util.MessagesCore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("data")
public class DataRecordsController {

    private final ServiceProvider sp;

    @PostMapping()
    public ResponseEntity<ResponseDTO> getRecordsByDatasetId(@RequestBody QueryDatasetDTO query) {
        return ResponseDTO.ok(sp.dataRecordsService.getPagedRecordsByQuery(query));
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseDTO> deleteRecordsByDatasetId(@RequestBody QueryDatasetDTO query) {
        return ResponseDTO.ok(sp.dataRecordsService.deleteRecordsByQuery(query));
    }

    @PostMapping("/deleteRecords")
    public ResponseEntity<ResponseDTO> deleteSelectedRecordsByDatasetId(@RequestBody SelectedRecordsToBeDeleted data) {
        sp.dataRecordsService.deleteSelectedRecordsByDatasetId(data);
        return ResponseDTO.ok(null);
    }

    @PostMapping("enrichment")
    public ResponseEntity<ResponseDTO> enrich(@RequestBody DatasetEnrichmentDTO datasetEnrichmentDTO) {
        String datasetId = sp.dataRecordsService.enrich(datasetEnrichmentDTO);
        return ResponseDTO.ok(new SingleValueDTO(datasetId), "Enrichment " + t(MessagesCore.SUCCESSFULLY_COMPLETED));
    }
}
