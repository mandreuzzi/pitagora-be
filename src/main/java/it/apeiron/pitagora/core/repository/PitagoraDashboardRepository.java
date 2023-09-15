package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.PitagoraDashboard;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraDashboardRepository extends MongoRepository<PitagoraDashboard, ObjectId> {
    boolean existsByName(String name);

}
