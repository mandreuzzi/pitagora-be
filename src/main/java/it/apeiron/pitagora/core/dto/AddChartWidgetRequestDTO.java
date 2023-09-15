package it.apeiron.pitagora.core.dto;

import lombok.Data;

@Data
public class AddChartWidgetRequestDTO {

    private String dashboardId;
    private QueryDatasetDTO query;
    private String columnNameX;
    private String columnNameY;
    private String size;

}
