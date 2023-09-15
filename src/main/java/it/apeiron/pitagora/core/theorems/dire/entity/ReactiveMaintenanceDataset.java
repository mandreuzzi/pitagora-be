package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import org.bson.types.ObjectId;

public class ReactiveMaintenanceDataset extends PitagoraDataset {

    public static ReactiveMaintenanceDataset create(ObjectId modelId) {
        return new ReactiveMaintenanceDataset(modelId);
    }

    private ReactiveMaintenanceDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmDireEntity.REACTIVE_MAINTENANCE.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.DIRE);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }
}
