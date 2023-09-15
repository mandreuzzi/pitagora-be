package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraMapperRepository extends MongoRepository<PitagoraMapper, ObjectId> {

    List<PitagoraMapper> findAllByReferenceSourceId(ObjectId sourceId);
    List<PitagoraMapper> findAllByReferenceModelId(ObjectId modelId);
    List<PitagoraMapper> findAllByReferenceSourceIdAndReferenceModelId(ObjectId sourceId, ObjectId modelId);

    boolean existsByName(String name);
    Optional<PitagoraMapper> findByName(String name);

}
