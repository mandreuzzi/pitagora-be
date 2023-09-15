package it.apeiron.pitagora.core.controller;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED_F;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_DELETED_F;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_UPDATED_F;

import it.apeiron.pitagora.core.dto.AddChartWidgetRequestDTO;
import it.apeiron.pitagora.core.dto.DashboardDTO;
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
@RequestMapping("dashboard")
public class DashboardController {

    private final ServiceProvider sp;

    @GetMapping
    public ResponseEntity<ResponseDTO> getAllDatasetAlarmsDetail() {
        return ResponseDTO.ok(sp.dashboardService.findAll());
    }

    @GetMapping("/{dashboardId}")
    public ResponseEntity<ResponseDTO> getByDatasetId(@PathVariable("dashboardId") ObjectId dashboardId) {
        return ResponseDTO.ok(sp.dashboardService.getDashboardById(dashboardId));
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> create(@RequestBody DashboardDTO dto) {
        sp.dashboardService.create(dto);
        return ResponseDTO.created("Dashboard " + t(SUCCESSFULLY_CREATED_F));
    }

    @PutMapping
    public ResponseEntity<ResponseDTO> update(@RequestBody DashboardDTO dto) {
        sp.dashboardService.update(dto);
        return ResponseDTO.created("Dashboard " + t(SUCCESSFULLY_UPDATED_F));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> delete(@PathVariable("id") ObjectId id) {
        sp.dashboardService.delete(id);
        return ResponseDTO.ok(null, "Dashboard " + t(SUCCESSFULLY_DELETED_F));
    }

    @PostMapping("/widget")
    public ResponseEntity<ResponseDTO> addWidget(@RequestBody AddChartWidgetRequestDTO dto) {
        sp.dashboardService.addWidget(dto);
        return ResponseDTO.created("Widget " + t(SUCCESSFULLY_CREATED));
    }

    @GetMapping("/widget/{dashboardId}")
    public ResponseEntity<ResponseDTO> getWidgetsByDatasetId(@PathVariable("dashboardId") ObjectId dashboardId) {
        return ResponseDTO.ok(sp.dashboardService.getWidgetsByDashboardId(dashboardId));
    }

}
