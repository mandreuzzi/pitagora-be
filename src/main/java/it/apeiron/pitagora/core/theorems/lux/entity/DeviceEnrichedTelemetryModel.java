package it.apeiron.pitagora.core.theorems.lux.entity;

import it.apeiron.pitagora.core.dto.DatasetEnrichmentDTO.AggregatedModelDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ModelService;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import java.util.LinkedHashMap;
import lombok.Data;

@Data
public class DeviceEnrichedTelemetryModel extends PitagoraModel {

    public static DeviceEnrichedTelemetryModel create() {
        return new DeviceEnrichedTelemetryModel();
    }

    private AggregatedModelDTO aggregatedModelDTO;

    private DeviceEnrichedTelemetryModel() {
        locked = true;
        this.name = ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.LUX);
        DeviceModel deviceModel = DeviceModel.create();
        TelemetryModel telemetryModel = TelemetryModel.create();
        structure = new LinkedHashMap<>();

        aggregatedModelDTO = ModelService
                .buildDefaultAggregateModelDTO(deviceModel, telemetryModel, "_telem_");
        aggregatedModelDTO.getFirstDataset().forEach(field -> structure.put(field.getTo().getName(), field.getTo()));
        aggregatedModelDTO.getSecondDataset().forEach(field -> structure.put(field.getTo().getName(), field.getTo()));

    }

}
