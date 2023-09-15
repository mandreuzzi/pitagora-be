package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraScopeCoordinates;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentsScopeCoordinatesRepo extends MongoRepository<PitagoraScopeCoordinates, String> {

    Optional<PitagoraScopeCoordinates> findByValueDescription_Value(String value);

}
