package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper.FieldMapperRule;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapperDTO extends AbstractRecordDTO {

    private String referenceSourceId;
    private String referenceModelId;
    private Map<String, FieldMapperRule> rules;
    private Object extraConfig;

    public static MapperDTO fromModel(PitagoraMapper m) {
        MapperDTO dto = MapperDTO.builder()
                .referenceSourceId(m.getReferenceSourceId().toString())
                .referenceModelId(m.getReferenceModelId().toString())
                .rules(m.getRules())
                .extraConfig(m.getExtraConfig())
                .build();
        dto.setSuperProps(m);
        return dto;
    }
}
