package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraAlarmEvent;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraAlarmEventRepository extends MongoRepository<PitagoraAlarmEvent, ObjectId> {

    List<PitagoraAlarmEvent> findAllByAlarmId(ObjectId alarmId);

    Optional<PitagoraAlarmEvent> findFirstByAlarmIdOrderByCreatedAtDesc(ObjectId alarmId);

    int countAllByDatasetId(ObjectId datasetId);

    void deleteAllByDatasetId(ObjectId datasetId);

    @Aggregation(pipeline = {
            "{'$lookup':{'from':'dataset','localField':'datasetId','foreignField':'_id',as:'dataset'}}",
    })
    List<PitagoraAlarmEvent> findWithDatasets();
    @Aggregation(pipeline = {
            "{'$lookup':{'from':'dataset','localField':'datasetId','foreignField':'_id',as:'dataset'}}",
            "{'$lookup':{'from':'alarm','localField':'alarmId','foreignField':'_id',as:'alarm'}}",
    })
    List<PitagoraAlarmEvent> findWithDatasetsAndAlarms();
}
