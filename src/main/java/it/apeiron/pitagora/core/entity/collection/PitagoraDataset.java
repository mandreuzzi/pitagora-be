package it.apeiron.pitagora.core.entity.collection;

import it.apeiron.pitagora.core.dto.DatasetCreationDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@NoArgsConstructor
@Document("dataset")
public class PitagoraDataset extends AbstractPitagoraRecord {

    protected ObjectId modelId;
    private List<DatasetGenerator> generators = new ArrayList<>();
    private List<PitagoraModel> model;

    public PitagoraDataset(DatasetCreationDTO dto) {
        update(dto);
    }

    public void update(DatasetCreationDTO d) {
        superUpdate(d);
        if (generators.isEmpty()) {
            modelId = new ObjectId(d.getModelId());
        }
    }

    public PitagoraModel getModel() {
        return model != null && !model.isEmpty() ? model.get(0) : null;
    }


    @Data
    @Builder
    public static class DatasetGenerator {

        private ObjectId sourceId;
        private ObjectId mapperId;
        private LocalDateTime generatedAt;

        public static DatasetGenerator create(String sourceId, String mapperId) {
            return DatasetGenerator.builder()
                    .sourceId(new ObjectId(sourceId))
                    .mapperId(new ObjectId(mapperId))
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }
}
