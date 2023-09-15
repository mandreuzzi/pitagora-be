package it.apeiron.pitagora.core.theorems.logico;

import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.logico.entity.BookingDataset;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("thm/logico")
public class ThmLogicoController {

    private final ServiceProvider sp;

    @GetMapping("/datasetId")
    public ResponseEntity<ResponseDTO> getSystemDatasetIdByName() {
        return ResponseDTO.ok(sp.datasetService.findByName(BookingDataset.BOOKING_DATASET_NAME).get().getId().toString());
    }
}
