package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.ModelField;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetTableDataDTO {
    private List<Map<String, Object>> data;
    private List<ModelField> fields;
    private PaginationDTO page;
    private String datasetName;
}
