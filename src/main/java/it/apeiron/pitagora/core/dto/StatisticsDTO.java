package it.apeiron.pitagora.core.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {
    private List<ValueDescriptionDTO> stats;
    private String columnDescription;

    public StatisticsDTO(String columnDescription) {
        this.columnDescription = columnDescription;
        this.stats = new ArrayList<>();
    }
}
