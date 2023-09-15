package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraHttpResponse;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraHttpResponseRepository extends MongoRepository<PitagoraHttpResponse, ObjectId> {

}
