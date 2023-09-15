package it.apeiron.pitagora.core.theorems.lux.entity;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import java.util.Arrays;
import java.util.LinkedHashMap;
import lombok.Data;

@Data
public class TelemetryModel extends PitagoraModel {

    public static TelemetryModel create() {
        return new TelemetryModel();
    }

    private TelemetryModel() {
        locked = true;
        this.name = ThmLuxEntity.TELEMETRY.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.LUX);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                ModelField.builder().name("_id").description("id").type(FieldType.STRING).build(),
                ModelField.builder().name("temperaturePCB").description("temperaturePCB").type(FieldType.INTEGER).build(),
                ModelField.builder().name("temperatureLamp").description("temperatureLamp").type(FieldType.INTEGER).build(),
                ModelField.builder().name("pwm").description("pwm").type(FieldType.INTEGER).build(),
                ModelField.builder().name("ldrControl").description("ldrControl").type(FieldType.STRING).build(),
                ModelField.builder().name("systemOnOff").description("systemOnOff").type(FieldType.BOOLEAN).build(),
                ModelField.builder().name("profileOnOff").description("profileOnOff").type(FieldType.BOOLEAN).build(),
                ModelField.builder().name("current").description("current").type(FieldType.INTEGER).build(),
                ModelField.builder().name("voltage").description("voltage").type(FieldType.INTEGER).build(),
                ModelField.builder().name("ldrSetpoint").description("ldrSetpoint").type(FieldType.INTEGER).build(),
                ModelField.builder().name("ldr").description("ldr").type(FieldType.INTEGER).build(),
                ModelField.builder().name("potency").description("potency").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("date").description("date").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("_fk_device").description("_fk_device").type(FieldType.STRING).build()
        )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
