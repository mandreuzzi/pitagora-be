package it.apeiron.pitagora.core.dto.charts;


import it.apeiron.pitagora.core.entity.ModelField;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class SingleNumericChartDTO extends ChartsDTO {
    private ChartType chartType = ChartType.SINGLE_NUMERIC;
    private ModelField field;
    private List<Double> values;

    public SingleNumericChartDTO(ModelField field, List<Double> values) {
        this.field = field;
        this.values = values;
    }
}
