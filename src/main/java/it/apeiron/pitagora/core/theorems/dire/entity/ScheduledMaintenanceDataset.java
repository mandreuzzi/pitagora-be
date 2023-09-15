package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import org.bson.types.ObjectId;

public class ScheduledMaintenanceDataset extends PitagoraDataset {

    public static ScheduledMaintenanceDataset create(ObjectId modelId) {
        return new ScheduledMaintenanceDataset(modelId);
    }

    private ScheduledMaintenanceDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.DIRE);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
