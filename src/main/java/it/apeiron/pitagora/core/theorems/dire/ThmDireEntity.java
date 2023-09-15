package it.apeiron.pitagora.core.theorems.dire;

import static it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord.SYSTEM_RESOURCE_NAME_PREFIX;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.entity.EnergyElectricityConsumptionDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.EnergyElectricityConsumptionModel;
import it.apeiron.pitagora.core.theorems.dire.entity.EnergyElectricityExpensesDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.EnergyElectricityExpensesModel;
import it.apeiron.pitagora.core.theorems.dire.entity.EnergyGasConsumptionDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.EnergyGasConsumptionModel;
import it.apeiron.pitagora.core.theorems.dire.entity.ReactiveAccountingDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.ReactiveAccountingModel;
import it.apeiron.pitagora.core.theorems.dire.entity.ReactiveMaintenanceDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.ReactiveMaintenanceModel;
import it.apeiron.pitagora.core.theorems.dire.entity.ScheduledMaintenanceDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.ScheduledMaintenanceModel;
import it.apeiron.pitagora.core.theorems.dire.entity.SiteDataset;
import it.apeiron.pitagora.core.theorems.dire.entity.SiteModel;
import java.util.List;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;

@AllArgsConstructor
public enum ThmDireEntity {
    SITE("Sede"),
    SCHEDULED_MAINTENANCE("Manutenzione Programmata"),
    REACTIVE_MAINTENANCE("Manutenzione Reattiva"),
    REACTIVE_ACCOUNTING("Contabilità Reattiva"),
    ENERGY_ELECTRICITY_CONSUMPTION("Consumo Elettrico"),
    ENERGY_ELECTRICITY_EXPENSES("Costo Energia Elettrica"),
    ENERGY_GAS_CONSUMPTION("Consumo E Costi Gas");

    private final String pitagoraName;

    public String pitagoraName() {
        return SYSTEM_RESOURCE_NAME_PREFIX + pitagoraName;
    }

    public void install(ServiceProvider sp) {
        PitagoraModel model = sp.modelService.findByName(pitagoraName())
                .orElse(sp.mongoTemplate.save(_createModel(),
                        sp.mongoTemplate.getCollectionName(PitagoraModel.class)));
        sp.mongoTemplate.save(_createDataset(model.getId()),
                sp.mongoTemplate.getCollectionName(PitagoraDataset.class));
    }

    private PitagoraModel _createModel() {
        switch (this) {
            case SITE:
                return SiteModel.create();
            case SCHEDULED_MAINTENANCE:
                return ScheduledMaintenanceModel.create();
            case REACTIVE_MAINTENANCE:
                return ReactiveMaintenanceModel.create();
            case REACTIVE_ACCOUNTING:
                return ReactiveAccountingModel.create();
            case ENERGY_GAS_CONSUMPTION:
                return EnergyGasConsumptionModel.create();
            case ENERGY_ELECTRICITY_CONSUMPTION:
                return EnergyElectricityConsumptionModel.create();
            case ENERGY_ELECTRICITY_EXPENSES:
                return EnergyElectricityExpensesModel.create();
            default:
                throw PitagoraException.internalServerError();
        }
    }

    private PitagoraDataset _createDataset(ObjectId modelId) {
        switch (this) {
            case SITE:
                return SiteDataset.create(modelId);
            case SCHEDULED_MAINTENANCE:
                return ScheduledMaintenanceDataset.create(modelId);
            case REACTIVE_MAINTENANCE:
                return ReactiveMaintenanceDataset.create(modelId);
            case REACTIVE_ACCOUNTING:
                return ReactiveAccountingDataset.create(modelId);
            case ENERGY_GAS_CONSUMPTION:
                return EnergyGasConsumptionDataset.create(modelId);
            case ENERGY_ELECTRICITY_CONSUMPTION:
                return EnergyElectricityConsumptionDataset.create(modelId);
            case ENERGY_ELECTRICITY_EXPENSES:
                return EnergyElectricityExpensesDataset.create(modelId);
            default:
                throw PitagoraException.internalServerError();
        }
    }

    public boolean isNotAboutEnergy() {
        return !List.of(
                ENERGY_ELECTRICITY_CONSUMPTION,
                ENERGY_GAS_CONSUMPTION)
                .contains(this);
    }
}
