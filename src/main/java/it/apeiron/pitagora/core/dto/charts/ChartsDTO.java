package it.apeiron.pitagora.core.dto.charts;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChartsDTO {

    protected String chartPreference;
    protected boolean reversed;

    public enum ChartType {
        HISTOGRAM, LINEAR, STACKED_HISTOGRAM, WHISKER, SINGLE_NUMERIC, MULTI_LINE
    }

}
