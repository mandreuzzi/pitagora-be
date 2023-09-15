package it.apeiron.pitagora.core.quartz;

import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraJobRepository extends MongoRepository<PitagoraJob, ObjectId> {

    List<PitagoraJob> findAllBySourceId(ObjectId sourceId);
    List<PitagoraJob> findAllByMapperId(ObjectId mapperId);
    List<PitagoraJob> findAllByDatasetId(ObjectId datasetId);

}
