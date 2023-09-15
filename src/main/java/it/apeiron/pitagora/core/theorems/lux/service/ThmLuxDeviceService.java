package it.apeiron.pitagora.core.theorems.lux.service;

import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;

import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import it.apeiron.pitagora.core.theorems.lux.dto.TelemetryDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmLuxDeviceService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public HttpResponseDTO getAllDevices() {
        return sp.thmLuxService.makeRequest(ThmLuxEntity.DEVICE.endpoint(), HttpMethod.GET);
    }

    public HttpResponseDTO setDimming(Integer level) {
        String SET_DIMMING_ENDPOINT = "/set_dimming";
        return sp.thmLuxService.makeRequest(SET_DIMMING_ENDPOINT, HttpMethod.POST,
                Map.of(
                        "device_serial", "8638",
                        "percentage", level,
                        "network_serial", "1")
        );
    }

    public Map<String, TelemetryDTO> getTelemetry(List<String> devicesName) {

        Map<String, TelemetryDTO> telemetry = new HashMap();
        List<AggregationOperation> aggregationPipeline = new ArrayList<>();
        aggregationPipeline.add(Aggregation.match(Criteria.where("data.name").in(devicesName)));
        aggregationPipeline.add(Aggregation.project()
                .andExpression("data.name").as("name")
                .andExpression("data._telem_date").as("date_long")
                .andExpression("data.lux").as("lux")
                .andExpression("data._telem_potency").as("power")
                .andExpression("data._telem_voltage").as("voltage")
                .andExpression("data._telem_current").as("current")
        );
        aggregationPipeline.add(Aggregation.addFields()
                .addField("date").withValue(toDate("$date_long"))
                .build());
        List<TelemetryDTO> results = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationPipeline), ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY
                        .pitagoraName(), TelemetryDTO.class).getMappedResults();

        devicesName.stream().filter(Objects::nonNull).forEach(name -> {
           results.stream().filter(res -> name.equals(res.getName()) && res.getDate() != null).max(Comparator.comparing(TelemetryDTO::getDate))
           .ifPresent(telemetryDTO -> telemetry.put(name, telemetryDTO));
        });
        return telemetry;
    }
}
