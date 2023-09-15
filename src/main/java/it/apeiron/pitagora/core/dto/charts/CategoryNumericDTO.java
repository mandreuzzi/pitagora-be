package it.apeiron.pitagora.core.dto.charts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryNumericDTO {
    private double value;
    private String category;
}
