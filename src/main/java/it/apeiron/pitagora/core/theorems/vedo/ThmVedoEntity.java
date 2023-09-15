package it.apeiron.pitagora.core.theorems.vedo;

import static it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord.SYSTEM_RESOURCE_NAME_PREFIX;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.vedo.entity.FaceDetectionDataset;
import it.apeiron.pitagora.core.theorems.vedo.entity.FaceDetectionModel;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;

@AllArgsConstructor
public enum ThmVedoEntity {
    FACE_DETECTION("Face Detection");

    private final String pitagoraName;

    public String pitagoraName() {
        return SYSTEM_RESOURCE_NAME_PREFIX + pitagoraName;
    }

    public void install(ServiceProvider sp) {
        PitagoraModel model = sp.modelService.findByName(pitagoraName)
                .orElse(sp.mongoTemplate.save(_createModel(),
                        sp.mongoTemplate.getCollectionName(PitagoraModel.class)));
        sp.mongoTemplate.save(_createDataset(model.getId()),
                sp.mongoTemplate.getCollectionName(PitagoraDataset.class));
    }

    private PitagoraModel _createModel() {
        switch (this) {
            case FACE_DETECTION:
                return FaceDetectionModel.create();
            default:
                throw PitagoraException.internalServerError();
        }
    }

    private PitagoraDataset _createDataset(ObjectId modelId) {
        switch (this) {
            case FACE_DETECTION:
                return FaceDetectionDataset.create(modelId);
            default:
                throw PitagoraException.internalServerError();
        }
    }

}
