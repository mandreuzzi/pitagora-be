package it.apeiron.pitagora.core.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DatasetCreationDTO extends AbstractRecordDTO {
    private String sourceId;
    private String mapperId;
    private String modelId;
}
