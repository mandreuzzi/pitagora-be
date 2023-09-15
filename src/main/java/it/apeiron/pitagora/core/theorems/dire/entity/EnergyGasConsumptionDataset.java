package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import org.bson.types.ObjectId;

public class EnergyGasConsumptionDataset extends PitagoraDataset {
    public static EnergyGasConsumptionDataset create(ObjectId modelId) {
        return new EnergyGasConsumptionDataset(modelId);
    }

    private EnergyGasConsumptionDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmDireEntity.ENERGY_GAS_CONSUMPTION.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.DIRE);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }
}
