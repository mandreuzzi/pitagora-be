package it.apeiron.pitagora.core.theorems.logico.entity;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class BookingModel extends PitagoraModel {

    public static final String BOOKING_MODEL_NAME = SYSTEM_RESOURCE_NAME_PREFIX + "Booking";

    public static BookingModel create() {
        return new BookingModel();
    }

    private BookingModel() {
        locked = true;
        this.name = BOOKING_MODEL_NAME;
        this.scope.add(Theorem.LOGICO);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        structure = new LinkedHashMap<>();
        Arrays.asList(
                ModelField.builder().name("ship").description("Nave").type(FieldType.STRING).build(),
                ModelField.builder().name("arrivalTime").description("Data consegna").type(FieldType.TIMESTAMP).build(),
                ModelField.builder().name("containerCode").description("Codice container").type(FieldType.STRING).build(),
                ModelField.builder().name("containerType").description("Tipo container").type(FieldType.STRING).build(),
                ModelField.builder().name("boxes").description("Colli").type(FieldType.STRING).build(),
                ModelField.builder().name("goods").description("Merce").type(FieldType.STRING).build(),
                ModelField.builder().name("netWeight").description("Peso netto").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("weight").description("Peso lordo").type(FieldType.DOUBLE).build(),
                ModelField.builder().name("destination").description("Destinazione").type(FieldType.STRING).build()
        )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
