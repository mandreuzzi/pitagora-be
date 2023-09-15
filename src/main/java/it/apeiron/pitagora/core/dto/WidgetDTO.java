package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.dto.charts.ChartsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetDTO {

    protected int indexPosition;
    protected String datasetName;
    protected String description;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartWidgetDTO extends WidgetDTO {
        private ChartsDTO chart;
        private QueryDatasetDTO query;
        private String columnNameX;
        private String columnNameY;
        private String size;
    }
}
