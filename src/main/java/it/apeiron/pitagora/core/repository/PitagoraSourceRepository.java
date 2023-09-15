package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraSourceRepository extends MongoRepository<PitagoraSource, ObjectId> {

    List<PitagoraSource> findAllByChannelOrderByCreatedAtDesc(SourceChannel channel);

    boolean existsByChannelAndName(SourceChannel channel, String name);

    Optional<PitagoraSource> findByName(String name);
}
