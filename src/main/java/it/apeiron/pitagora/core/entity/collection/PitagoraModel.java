package it.apeiron.pitagora.core.entity.collection;


import it.apeiron.pitagora.core.dto.ModelDTO;
import it.apeiron.pitagora.core.entity.ModelField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.LinkedHashMap;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("model")
public class PitagoraModel extends AbstractPitagoraRecord {

    protected LinkedHashMap<String, ModelField> structure;

    public PitagoraModel(ModelDTO dto) {
        update(dto);
    }

    public void update(ModelDTO d) {
        superUpdate(d);
        structure = new LinkedHashMap<>();

        d.getStructure().forEach(attr -> structure.put(attr.getName().trim(), attr));
    }

}
