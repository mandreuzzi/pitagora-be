package it.apeiron.pitagora.core.theorems.lux;

import static it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord.SYSTEM_RESOURCE_NAME_PREFIX;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.lux.entity.DeviceDataset;
import it.apeiron.pitagora.core.theorems.lux.entity.DeviceEnrichedTelemetryDataset;
import it.apeiron.pitagora.core.theorems.lux.entity.DeviceEnrichedTelemetryModel;
import it.apeiron.pitagora.core.theorems.lux.entity.DeviceMapper;
import it.apeiron.pitagora.core.theorems.lux.entity.DeviceModel;
import it.apeiron.pitagora.core.theorems.lux.entity.TelemetryDataset;
import it.apeiron.pitagora.core.theorems.lux.entity.TelemetryMapper;
import it.apeiron.pitagora.core.theorems.lux.entity.TelemetryModel;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;

@AllArgsConstructor
public enum ThmLuxEntity {
    DEVICE("Device", "/device", true),
    TELEMETRY("Telemetry", "/telemetry", true),
    DEVICE_ENRICHED_TELEMETRY("Device_Telemetry", null, false);

    private final String pitagoraName;
    private final String endpoint;
    private final boolean mustHaveAMapper;

    public String pitagoraName() {
        return SYSTEM_RESOURCE_NAME_PREFIX + pitagoraName;
    }
    public String endpoint() {
        return endpoint;
    }

    public void install(ServiceProvider sp) {
        PitagoraModel model = sp.modelService.findByName(pitagoraName())
                .orElse(sp.mongoTemplate.save(_createModel(), sp.mongoTemplate.getCollectionName(PitagoraModel.class)));
        if (mustHaveAMapper && sp.mapperService.findByName(pitagoraName()).isEmpty()) {
            sp.mongoTemplate.save(_createMapper(model), sp.mongoTemplate.getCollectionName(PitagoraMapper.class));
        }
        sp.mongoTemplate.save(_createDataset(model.getId()), sp.mongoTemplate.getCollectionName(PitagoraDataset.class));
    }

    private PitagoraModel _createModel() {
        switch (this) {
            case DEVICE:
                return DeviceModel.create();
            case TELEMETRY:
                return TelemetryModel.create();
            case DEVICE_ENRICHED_TELEMETRY:
                return DeviceEnrichedTelemetryModel.create();
            default:
                throw PitagoraException.internalServerError();
        }
    }

    private PitagoraMapper _createMapper(PitagoraModel model) {
        switch (this) {
            case DEVICE:
                return DeviceMapper.create(model);
            case TELEMETRY:
                return TelemetryMapper.create(model);
            default:
                throw PitagoraException.internalServerError();
        }
    }

    private PitagoraDataset _createDataset(ObjectId modelId) {
        switch (this) {
            case DEVICE:
                return DeviceDataset.create(modelId);
            case TELEMETRY:
                return TelemetryDataset.create(modelId);
            case DEVICE_ENRICHED_TELEMETRY:
                return DeviceEnrichedTelemetryDataset.create(modelId);
            default:
                throw PitagoraException.internalServerError();
        }
    }
}
