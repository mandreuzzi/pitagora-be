package it.apeiron.pitagora.core.theorems.lux.entity;


import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import org.bson.types.ObjectId;

public class DeviceEnrichedTelemetryDataset extends PitagoraDataset {

    public static DeviceEnrichedTelemetryDataset create(ObjectId modelId) {
        return new DeviceEnrichedTelemetryDataset(modelId);
    }

    private DeviceEnrichedTelemetryDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.LUX);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
