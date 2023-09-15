package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.ExposedApiSource;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExposedApiSourceDTO extends AbstractRecordDTO {

    private String datasetId;
    private String keyId;

    public static ExposedApiSourceDTO fromModel(PitagoraSource m) {
        ExposedApiSource c = m.getExposedApiConfiguration();
        ExposedApiSourceDTO dto = ExposedApiSourceDTO.builder()
                .datasetId(c.getDatasetId().toString())
                .build();

        dto.setSuperProps(m);
        return dto;
    }
}
