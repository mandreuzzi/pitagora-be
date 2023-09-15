package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import org.bson.types.ObjectId;


public class EnergyElectricityExpensesDataset extends PitagoraDataset {

    public static EnergyElectricityExpensesDataset create(ObjectId modelId) {
        return new EnergyElectricityExpensesDataset(modelId);
    }

    private EnergyElectricityExpensesDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmDireEntity.ENERGY_ELECTRICITY_EXPENSES.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.DIRE);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
