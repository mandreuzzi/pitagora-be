package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraModelRepository extends MongoRepository<PitagoraModel, ObjectId> {

    boolean existsByName(String name);

    Optional<PitagoraModel> findByName(String name);
}
