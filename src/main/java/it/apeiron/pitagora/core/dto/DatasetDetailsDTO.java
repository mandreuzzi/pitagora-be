package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DatasetDetailsDTO extends AbstractRecordDTO {
    private String modelId;
    private String modelName;
    private long records;
    private List<DatasetGeneratorDTO> generators;

    public static DatasetDetailsDTO fromModel(PitagoraDataset ds) {
        DatasetDetailsDTO dto = DatasetDetailsDTO.builder()
                .modelId(ds.getModelId().toString())
                .modelName(ds.getModel() != null ? ds.getModel().getName() : "")
                .build();
        dto.setSuperProps(ds);
        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DatasetGeneratorDTO {
        private String source;
        private String mapper;
        private LocalDateTime generatedAt;
    }
}
