package it.apeiron.pitagora.core.repository;

import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PitagoraUserRepository extends MongoRepository<PitagoraUser, ObjectId> {

    Optional<PitagoraUser> findPitagoraUserByEmail(String email);
    Optional<PitagoraUser> findByRole(Role role);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);

    @Query("{ 'lastName' : { $regex: ?0 } }")
    List<PitagoraUser> findByExample(String query);
}
