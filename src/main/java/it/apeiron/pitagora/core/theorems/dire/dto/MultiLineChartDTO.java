package it.apeiron.pitagora.core.theorems.dire.dto;

import it.apeiron.pitagora.core.dto.charts.ChartsDTO;
import it.apeiron.pitagora.core.dto.charts.NumericNumericDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class MultiLineChartDTO  extends ChartsDTO {
    private ChartType chartType = ChartType.MULTI_LINE;
    private Map<String, List<NumericNumericDTO>> data = new HashMap<>();

    public static MultiLineChartDTO buildFromKeysInteger(Map<Integer, List<NumericNumericDTO>> data) {
        MultiLineChartDTO chart = new MultiLineChartDTO();
        chart.data = new HashMap<>();
        data.forEach((k,v) -> chart.data.put(Integer.toString(k), v));
        return chart;
    }

}
