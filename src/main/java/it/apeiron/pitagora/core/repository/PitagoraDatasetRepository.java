package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraDatasetRepository extends MongoRepository<PitagoraDataset, ObjectId> {

    boolean existsByName(String name);

    List<PitagoraDataset> findAllByModelId(ObjectId modelId);

    Optional<PitagoraDataset> findByName(String name);

}
