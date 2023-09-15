package it.apeiron.pitagora.core.entity;

import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.dto.ExposedApiSourceDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraExposedApiKey;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.repository.PitagoraExposedApiKeyRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExposedApiSource implements ISource {

    private ObjectId datasetId;
    private ObjectId keyId;

    public ExposedApiSource(AbstractRecordDTO a, SourceChannel channel, MongoRepository repository) {
        ExposedApiSourceDTO d = (ExposedApiSourceDTO) a;
        keyId = new ObjectId(d.getKeyId());

        PitagoraExposedApiKeyRepository exposedApiKeyRepository = (PitagoraExposedApiKeyRepository) repository;
        PitagoraExposedApiKey key = exposedApiKeyRepository
                .findById(keyId).get();
        key.setEnabled(true);
        exposedApiKeyRepository.save(key);

        update(d, channel, repository);
    }

    @Override
    public void update(AbstractRecordDTO a, SourceChannel channel, MongoRepository repository) {
        ExposedApiSourceDTO d = (ExposedApiSourceDTO) a;
        datasetId = new ObjectId(d.getDatasetId());
    }
}
