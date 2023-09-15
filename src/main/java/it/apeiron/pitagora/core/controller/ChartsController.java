package it.apeiron.pitagora.core.controller;


import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.dto.charts.RequestChartDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
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
@RequestMapping("chart")
public class ChartsController {

    private final ServiceProvider sp;

    @PostMapping
    public ResponseEntity<ResponseDTO> getChart(@RequestBody RequestChartDTO requestChart) {
        return ResponseDTO.ok(sp.chartsService.getChart(requestChart));
    }

}
