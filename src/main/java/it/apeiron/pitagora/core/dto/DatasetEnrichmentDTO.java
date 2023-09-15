package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.ModelField;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class DatasetEnrichmentDTO {

    private String modelName;
    private AggregatedModelDTO modelStructure;
    private String datasetName;
    private String firstDatasetId;
    private String firstDatasetField;
    private List<Filter> firstDatasetFilters;
    private String secondDatasetId;
    private String secondDatasetField;
    private List<Filter> secondDatasetFilters;
    private LocalDateTime firstDatasetStartingDate;
    private LocalDateTime firstDatasetEndingDate;
    private LocalDateTime secondDatasetStartingDate;
    private LocalDateTime secondDatasetEndingDate;

    @Data
    public static class AggregatedModelDTO {
        private List<AggregatedModelFieldDTO> firstDataset;
        private List<AggregatedModelFieldDTO> secondDataset;
    }

    @Data
    @NoArgsConstructor
    public static class AggregatedModelFieldDTO {
        private ModelField from;
        private ModelField to;

        public AggregatedModelFieldDTO(ModelField field) {
            from = field;
            to = field;
        }
    }
}
