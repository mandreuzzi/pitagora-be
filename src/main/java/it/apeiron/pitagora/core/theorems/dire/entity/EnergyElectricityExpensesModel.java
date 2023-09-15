package it.apeiron.pitagora.core.theorems.dire.entity;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import lombok.Data;

import java.util.Arrays;
import java.util.LinkedHashMap;


@Data

public class EnergyElectricityExpensesModel extends PitagoraModel {

    public static EnergyElectricityExpensesModel create() {
        return new EnergyElectricityExpensesModel();
    }

    private EnergyElectricityExpensesModel() {
        locked = true;
        this.name = ThmDireEntity.ENERGY_ELECTRICITY_EXPENSES.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.DIRE);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                        ModelField.builder().name("anno").description("Anno").type(FieldType.INTEGER).build(),
                        ModelField.builder().name("sede").description("Sede").type(FieldType.STRING).build(),
                        ModelField.builder().name("idContatore").description("Id. Contatore").type(FieldType.STRING).build(),
                        ModelField.builder().name("periodo").description("Periodo").type(FieldType.TIMESTAMP).build(),
                        ModelField.builder().name("costoII").description("Costo I.I").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("costoEnergiaIE").description("Costo Energia I.E").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("consumo").description("Consumo Kw/H").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("costoNetto").description("Costo Kwh Netto").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("costoLordo").description("Costo Kwh Lordo").type(FieldType.DOUBLE).build(),
                        ModelField.builder().name("indirizzo").description("Indirizzo").type(FieldType.STRING).build()
                )
                .forEach(field -> structure.put(field.getName(), field));
    }
}
