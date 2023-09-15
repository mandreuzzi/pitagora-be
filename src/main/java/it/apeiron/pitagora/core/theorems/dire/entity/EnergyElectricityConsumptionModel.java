package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import java.util.Arrays;
import java.util.LinkedHashMap;
import lombok.Data;

@Data
public class EnergyElectricityConsumptionModel extends PitagoraModel {

    public static EnergyElectricityConsumptionModel create() {
        return new EnergyElectricityConsumptionModel();
    }

    private EnergyElectricityConsumptionModel() {
        locked = true;
        this.name = ThmDireEntity.ENERGY_ELECTRICITY_CONSUMPTION.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.DIRE);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                        ModelField.builder().name("pivaCfAnnoMeseCompetenza").description("PIVA/CF | Anno Mese Competenza (aaaamm)").type(FieldType.STRING).build(),
                        ModelField.builder().name("podPdr").description("POD/PDR").type(FieldType.STRING).build(),
                        ModelField.builder().name("nrUtente").description("Nr Utente").type(FieldType.STRING).build(),
                        ModelField.builder().name("provincia").description("Provincia").type(FieldType.STRING).build(),
                        ModelField.builder().name("sede").description("Sede").type(FieldType.STRING).build(),
                        ModelField.builder().name("peak").description("Peak (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("offPeak").description("OffPeak (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("f1").description("F1 (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("f2").description("F2 (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("f3").description("F3 (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("energiaMese").description("Energia Mese (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("energia").description("Energia (kWh)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("potenzaMax").description("Potenza Max (kW)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("data").description("Data").type(FieldType.TIMESTAMP).build()
                )
                .forEach(field -> structure.put(field.getName(), field));
    }
}
