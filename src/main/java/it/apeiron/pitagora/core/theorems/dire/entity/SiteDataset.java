package it.apeiron.pitagora.core.theorems.dire.entity;


import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import org.bson.types.ObjectId;

public class SiteDataset extends PitagoraDataset {

    public static SiteDataset create(ObjectId modelId) {
        return new SiteDataset(modelId);
    }

    private SiteDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmDireEntity.SITE.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.DIRE);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
