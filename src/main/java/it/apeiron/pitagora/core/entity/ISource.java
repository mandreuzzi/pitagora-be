package it.apeiron.pitagora.core.entity;

import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ISource {
    void update(AbstractRecordDTO dto, SourceChannel channel, MongoRepository repositoryanguage);
}
