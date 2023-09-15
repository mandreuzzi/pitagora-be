package it.apeiron.pitagora.core.dto.charts;

import static it.apeiron.pitagora.core.entity.enums.FieldType.BOOLEAN;
import static it.apeiron.pitagora.core.entity.enums.FieldType.DOUBLE;
import static it.apeiron.pitagora.core.entity.enums.FieldType.INTEGER;
import static it.apeiron.pitagora.core.entity.enums.FieldType.STRING;
import static it.apeiron.pitagora.core.entity.enums.FieldType.TIMESTAMP;

import it.apeiron.pitagora.core.entity.ModelField;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDataDTO {
    private ModelField field;
    private List<Object> values;

    public FieldDataDTO(ModelField field) {
        this.field = field;
        values = new ArrayList<>();
    }

    public boolean isNumerical() {
        return INTEGER.equals(this.field.getType()) || DOUBLE.equals(this.field.getType()) || TIMESTAMP.equals(this.field.getType());
    }

    public boolean isCategorical() {
        return STRING.equals(this.field.getType()) || BOOLEAN.equals(this.field.getType());
    }

    public boolean isTimestamp() {
        return TIMESTAMP.equals(this.field.getType());
    }
}
