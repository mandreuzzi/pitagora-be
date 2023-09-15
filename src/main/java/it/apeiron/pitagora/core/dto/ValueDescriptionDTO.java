package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.AbstractPitagoraRecord;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValueDescriptionDTO {

    private String value;
    private String description;
    private boolean disabled;
    private String type;

    public ValueDescriptionDTO(String valueDescription) {
        this.value = valueDescription;
        this.description = this.value;
    }

    public ValueDescriptionDTO(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public ValueDescriptionDTO(String value, String description, String type) {
        this.value = value;
        this.description = description;
        this.type = type;
    }

    public static ValueDescriptionDTO fromResource(AbstractPitagoraRecord a) {
        return ValueDescriptionDTO.builder()
                .value(a.getId().toString())
                .description(a.getNameForClient())
                .type((a instanceof PitagoraSource) ? ((PitagoraSource) a).getChannel().name() : null)
                .build();
    }
}
