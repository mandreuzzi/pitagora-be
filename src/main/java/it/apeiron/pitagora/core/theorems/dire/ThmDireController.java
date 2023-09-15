package it.apeiron.pitagora.core.theorems.dire;

import it.apeiron.pitagora.core.dto.ExportRequestDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.dto.BimDetailsRequestDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.MaintenanceRequestDTO;
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
@RequestMapping("thm/dire")
public class ThmDireController {

    private final ServiceProvider sp;

    @GetMapping("/datasetId")
    public ResponseEntity<ResponseDTO> getSystemDatasetIdByName() {
        return ResponseDTO.ok(sp.datasetService.findByName(ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName()).get().getId().toString());
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> getAllYearsAndAddresses() {
        return ResponseDTO.ok(sp.thmDireService.getAllYearsAndAddresses());
    }

    @PostMapping("/maintenance/scheduled")
    public ResponseEntity<ResponseDTO> getScheduledMaintenance(@RequestBody MaintenanceRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireMaintenanceService.getScheduledMaintenance(dto));
    }

    @PostMapping("/maintenance/reactive")
    public ResponseEntity<ResponseDTO> getReactiveMaintenance(@RequestBody MaintenanceRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireMaintenanceService.getReactiveMaintenance(dto));
    }

    @PostMapping("/energy/electricity")
    public ResponseEntity<ResponseDTO> getElectricityTab(@RequestBody MaintenanceRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireEnergyService.getElectricityTab(dto));
    }

    @PostMapping("/energy/gas")
    public ResponseEntity<ResponseDTO> getGasTab(@RequestBody MaintenanceRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireEnergyService.getGasTab(dto));
    }

    @PostMapping("/sustainability")
    public ResponseEntity<ResponseDTO> getSustainabilityTab(@RequestBody MaintenanceRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireSustainabilityService.getSustainabilityTab(dto));
    }

    @PostMapping("/finance")
    public ResponseEntity<ResponseDTO> getFinanceTab(@RequestBody MaintenanceRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireFinanceService.getFinanceTab(dto));
    }

    @PutMapping("/{tab}")
    public ResponseEntity<ResponseDTO> updatePreferences(@PathVariable (name = "tab") String tab, @RequestBody Object preferences) {
        sp.thmDireService.updateTabPreferences(tab, preferences);
        return ResponseDTO.ok("Ok");
    }

    @PostMapping("/bim")
    public ResponseEntity<ResponseDTO> getScheduledMaintenanceForBim(@RequestBody BimDetailsRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireBimService.getBimDetails(dto));
    }

    @PostMapping("/export")
    public ResponseEntity<ResponseDTO> export(@RequestBody ExportRequestDTO dto) {
        return ResponseDTO.ok(sp.thmDireExportService.export(dto));
    }

}
