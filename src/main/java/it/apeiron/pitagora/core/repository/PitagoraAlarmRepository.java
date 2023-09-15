package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraAlarm;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraAlarmRepository extends MongoRepository<PitagoraAlarm, ObjectId> {

    List<PitagoraAlarm> findAllByDatasetId(ObjectId datasetId);

    boolean existsByEnabledAndDatasetId(boolean action, ObjectId datasetId);

    int countAllByDatasetId(ObjectId datasetId);

    void deleteAllByDatasetId(ObjectId datasetId);

}
