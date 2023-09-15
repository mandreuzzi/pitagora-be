package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.util.LinkedList;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModelDTO extends AbstractRecordDTO {

    private LinkedList<ModelField> structure;

    public static ModelDTO fromModel(PitagoraModel model) {
        ModelDTO dto = ModelDTO.builder()
                .structure(new LinkedList<>(model.getStructure().values()))
                .build();
        dto.setSuperProps(model);
        return dto;
    }
}
