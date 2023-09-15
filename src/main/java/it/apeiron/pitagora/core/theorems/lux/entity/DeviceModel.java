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
public class DeviceModel extends PitagoraModel {

    public static DeviceModel create() {
        return new DeviceModel();
    }

    private DeviceModel() {
        locked = true;
        this.name = ThmLuxEntity.DEVICE.pitagoraName();
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        this.scope.add(Theorem.LUX);
        structure = new LinkedHashMap<>();

        Arrays.asList(
                ModelField.builder().name("_id").description("id").type(FieldType.STRING).build(),
                ModelField.builder().name("name").description("Name").type(FieldType.STRING).build(),
                ModelField.builder().name("statusSystem").description("Status System").type(FieldType.STRING).build(),
                ModelField.builder().name("latitude").description("Latitude").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("longitude").description("Longitude").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("dt_create").description("Date Creation").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("dateSubmitted").description("Date Submitted").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("sunset").description("sunset").type(FieldType.STRING).build(),
                ModelField.builder().name("sunrise").description("sunrise").type(FieldType.STRING).build(),
                ModelField.builder().name("power").description("power").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("dimmingPower").description("dimmingPower").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("note").description("note").type(FieldType.STRING).build(),
                ModelField.builder().name("serial").description("serial").type(FieldType.STRING).build(),
                ModelField.builder().name("profile_active").description("profile_active").type(FieldType.STRING).build(),
                ModelField.builder().name("statusLDR").description("statusLDR").type(FieldType.STRING).build(),
                ModelField.builder().name("lux").description("lux").type(FieldType.INTEGER).build(),
                ModelField.builder().name("gestaoDimming").description("gestaoDimming").type(FieldType.INTEGER).build(),
                ModelField.builder().name("time").description("time").type(FieldType.STRING).build(),
                ModelField.builder().name("statusAlarm").description("statusAlarm").type(FieldType.BOOLEAN).build(),
                ModelField.builder().name("noReply").description("noReply").type(FieldType.BOOLEAN).build(),
                ModelField.builder().name("dateNoReply").description("Date NoReply").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("dateReply").description("Date Reply").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("pm").description("pm").type(FieldType.STRING).build(),
                ModelField.builder().name("wm").description("wm").type(FieldType.STRING).build()
        )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
