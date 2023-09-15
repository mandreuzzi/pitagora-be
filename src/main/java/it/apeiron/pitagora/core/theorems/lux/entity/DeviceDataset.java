package it.apeiron.pitagora.core.theorems.lux.entity;


import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import org.bson.types.ObjectId;

public class DeviceDataset extends PitagoraDataset {

    public static DeviceDataset create(ObjectId modelId) {
        return new DeviceDataset(modelId);
    }

    private DeviceDataset(ObjectId modelId) {
        locked = true;
        this.name = ThmLuxEntity.DEVICE.pitagoraName();
        this.modelId = modelId;
        this.scope.add(Theorem.LUX);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
