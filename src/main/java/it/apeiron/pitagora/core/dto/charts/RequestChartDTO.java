package it.apeiron.pitagora.core.dto.charts;

import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestChartDTO {
    private String columnNameX;
    private String columnNameY;
    private QueryDatasetDTO query;
}
