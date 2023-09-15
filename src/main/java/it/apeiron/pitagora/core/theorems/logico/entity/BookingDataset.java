package it.apeiron.pitagora.core.theorems.logico.entity;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import org.bson.types.ObjectId;

public class BookingDataset extends PitagoraDataset {

    public static final String BOOKING_DATASET_NAME = SYSTEM_RESOURCE_NAME_PREFIX + "Booking";

    public static BookingDataset create(ObjectId modelId) {
        return new BookingDataset(modelId);
    }

    private BookingDataset(ObjectId modelId) {
        locked = true;
        this.name = BOOKING_DATASET_NAME;
        this.modelId = modelId;
        this.scope.add(Theorem.LOGICO);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
    }

}
