package it.apeiron.pitagora.core.entity;

import it.apeiron.pitagora.core.dto.QueryDatasetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Widget {

    protected int indexPosition;

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartWidget extends Widget{
        private String columnNameX;
        private String columnNameY;
        private QueryDatasetDTO query;
        private String size;
    }
}
