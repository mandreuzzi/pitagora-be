package it.apeiron.pitagora.core.theorems.lux;

import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("thm/lux")
public class ThmLuxController {

    private final ServiceProvider sp;

    @GetMapping("/credz")
    public ResponseEntity<ResponseDTO> getCredentials() {
        return ResponseDTO.ok(sp.thmLuxService.getCredentials());
    }

    @GetMapping("/device/datasetId")
    public ResponseEntity<ResponseDTO> getDeviceDatasetIdByName() {
        return ResponseDTO.ok(sp.datasetService.findByName(ThmLuxEntity.DEVICE.pitagoraName()).get().getId().toString());
    }
    @GetMapping("/telemetry/datasetId")
    public ResponseEntity<ResponseDTO> getTelemetryDatasetIdByName() {
        return ResponseDTO.ok(sp.datasetService.findByName(ThmLuxEntity.TELEMETRY.pitagoraName()).get().getId().toString());
    }
    @GetMapping("/deviceWithTelemetry/datasetId")
    public ResponseEntity<ResponseDTO> getDeviceWithTelemetryDatasetIdByName() {
        return ResponseDTO.ok(sp.datasetService.findByName(ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY.pitagoraName()).get().getId().toString());
    }
    @PostMapping("/device/telemetry")
    public ResponseEntity<ResponseDTO> getTelemetry(@RequestBody List<String> devicesName) {
        return ResponseDTO.ok(sp.thmLuxDeviceService.getTelemetry(devicesName));
    }
    @GetMapping("/device")
    public ResponseEntity<ResponseDTO> getAllDevices() {
        return ResponseDTO.ok(sp.thmLuxDeviceService.getAllDevices());
    }

    @PostMapping("/set_dimming/{level}")
    public ResponseEntity<ResponseDTO> setDimming(@PathVariable Integer level) {
        return ResponseDTO.ok(sp.thmLuxDeviceService.setDimming(level));
    }
}
