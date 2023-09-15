package it.apeiron.pitagora.core.theorems.lux.entity;


import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import org.bson.types.ObjectId;

public class TelemetryDataset extends PitagoraDataset {

    public static TelemetryDataset create(ObjectId modelId) {
        return new TelemetryDataset(modelId);
    }

    private TelemetryDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmLuxEntity.TELEMETRY.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.LUX);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
