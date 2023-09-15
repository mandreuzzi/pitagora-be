package it.apeiron.pitagora.core.controller;


import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.dto.charts.RequestChartDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("statistics")
public class StatisticsController {

    private final ServiceProvider sp;

    @PostMapping("/{columnName}")
    public ResponseEntity<ResponseDTO> getStatistics(@PathVariable("columnName") String columnName, @RequestBody QueryDatasetDTO query) {
        return ResponseDTO.ok(sp.statisticsService.getStatistics(columnName, query));
    }

    @PostMapping("/linear")
    public ResponseEntity<ResponseDTO> getLinearRegression(@RequestBody RequestChartDTO dto) {
        return ResponseDTO.ok(sp.statisticsService.getLinearRegression(dto));
    }
}
