package it.apeiron.pitagora.core.theorems.vedo.entity;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import org.bson.types.ObjectId;

public class FaceDetectionDataset extends PitagoraDataset {

    public static final String FACE_DETECTION_DATASET_NAME = SYSTEM_RESOURCE_NAME_PREFIX + "Face Detection";

    public static FaceDetectionDataset create(ObjectId modelId) {
        return new FaceDetectionDataset(modelId);
    }

    private FaceDetectionDataset(ObjectId modelId) {
        locked = true;
        this.name = FACE_DETECTION_DATASET_NAME;
        this.modelId = modelId;
        this.scope.add(Theorem.VEDO);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
