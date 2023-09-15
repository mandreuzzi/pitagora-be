package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class LinePointDTO {

    private long date;
    private Double value;

    public LinePointDTO(Double value, long date) {
        this.value = value;
        this.date = date;
    }
}
