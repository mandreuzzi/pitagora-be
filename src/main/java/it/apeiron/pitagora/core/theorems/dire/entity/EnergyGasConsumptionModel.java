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
public class EnergyGasConsumptionModel extends PitagoraModel {

    public static EnergyGasConsumptionModel create() {
        return new EnergyGasConsumptionModel();
    }

    private EnergyGasConsumptionModel() {
        locked = true;
        this.name = ThmDireEntity.ENERGY_GAS_CONSUMPTION.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.DIRE);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                        ModelField.builder().name("periodo").description("Periodo").type(FieldType.STRING).build(),
                        ModelField.builder().name("numeroImpianto").description("Numero Impianto").type(FieldType.STRING).build(),
                        ModelField.builder().name("pdr").description("PdR").type(FieldType.STRING).build(),
                        ModelField.builder().name("indirizzoDiFornitura").description("Indirizzo di Fornitura").type(FieldType.STRING).build(),
                        ModelField.builder().name("att").description("Att").type(FieldType.STRING).build(),
                        ModelField.builder().name("localita").description("Località").type(FieldType.STRING).build(),
                        ModelField.builder().name("ricaviVendita").description("Ricavi Vendita (Doc calcolo)").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("quantitaGas").description("Quantità GAS").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("sede").description("Sede").type(FieldType.STRING).build(),
                        ModelField.builder().name("data").description("Data").type(FieldType.TIMESTAMP).build(),
                        ModelField.builder().name("coefficenteDiConversioneInKwh").description("Coefficiente di conversione mc in kWh eq").type(FieldType.DOUBLE).build()
                )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
