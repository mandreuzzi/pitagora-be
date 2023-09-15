package it.apeiron.pitagora.core.entity;

import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Operation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Filter {
    private String field;
    private FieldType fieldType;
    private Operation operation;
    private String value;

    public void setValue(String value) {
        this.value = value.trim();
    }
}
