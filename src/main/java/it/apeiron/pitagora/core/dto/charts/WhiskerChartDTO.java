package it.apeiron.pitagora.core.dto.charts;


import it.apeiron.pitagora.core.entity.ModelField;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class WhiskerChartDTO extends ChartsDTO {
    private ChartType chartType = ChartType.WHISKER;
    private ModelField categoryField;
    private ModelField valueField;
    private List<CategoryNumericDTO> categoryNumeric;

    public WhiskerChartDTO(ModelField categoryField, ModelField valueField, List<CategoryNumericDTO> categoryNumeric) {
        this.categoryField = categoryField;
        this.valueField = valueField;
        this.categoryNumeric = categoryNumeric;
    }
}
