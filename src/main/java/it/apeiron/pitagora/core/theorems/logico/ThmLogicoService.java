package it.apeiron.pitagora.core.theorems.logico;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.logico.entity.BookingDataset;
import it.apeiron.pitagora.core.theorems.logico.entity.BookingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;


@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmLogicoService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
        _init();
    }

    private void _init() {
        if (sp.datasetService.findByName(BookingDataset.BOOKING_DATASET_NAME).isEmpty()) {
            PitagoraModel model = sp.modelService.findByName(BookingModel.BOOKING_MODEL_NAME)
                    .orElse(sp.mongoTemplate.save(BookingModel.create(),
                            sp.mongoTemplate.getCollectionName(PitagoraModel.class)));
            sp.mongoTemplate.save(BookingDataset.create(model.getId()), sp.mongoTemplate.getCollectionName(PitagoraDataset.class));
        }
    }

}
