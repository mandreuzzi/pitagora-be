package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraFile;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraFileRepository extends MongoRepository<PitagoraFile, ObjectId> {

    Optional<PitagoraFile> findByHashCode(String hash);

    PitagoraFile findByFileName(String videoId);
}
