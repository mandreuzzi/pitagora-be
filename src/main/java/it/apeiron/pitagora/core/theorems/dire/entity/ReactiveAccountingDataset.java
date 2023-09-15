package it.apeiron.pitagora.core.theorems.dire.entity;


import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import org.bson.types.ObjectId;

public class ReactiveAccountingDataset extends PitagoraDataset {

    public static ReactiveAccountingDataset create(ObjectId modelId) {
        return new ReactiveAccountingDataset(modelId);
    }

    private ReactiveAccountingDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmDireEntity.REACTIVE_ACCOUNTING.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.DIRE);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }
}
