package it.apeiron.pitagora.core.dto.charts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.apeiron.pitagora.core.entity.ModelField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


@Data
@NoArgsConstructor
@SuperBuilder
public class LinearChartDTO extends ChartsDTO {
    private ChartType chartType = ChartType.LINEAR;
    private ModelField valueFieldX;
    private ModelField valueFieldY;
    private List<NumericNumericDTO> numericNumeric;
    private List<NumericNumericDTO> linearRegression;
    private List<NumericNumericDTO> meanXValues;
    private List<NumericNumericDTO> meanYValues;
    private Double total;
    private Double currentYearVariationComparedToPreviousYear;
    private List<NumericNumericDTO> prediction;

    @JsonIgnore
    double[] xArr;
    @JsonIgnore
    double[] yArr;
    @JsonIgnore
    private DescriptiveStatistics _xStats;
    @JsonIgnore
    private DescriptiveStatistics _yStats;


    public LinearChartDTO(ModelField valueFieldX, ModelField valueFieldY, List<NumericNumericDTO> numericNumeric) {
        this.valueFieldX = valueFieldX;
        this.valueFieldY = valueFieldY;
        this.numericNumeric = numericNumeric;
    }


    public LinearChartDTO numericNumeric(List<NumericNumericDTO> numericNumeric) {
        this.numericNumeric = numericNumeric;
        return this;
    }

    private void _prepareData() {
        xArr = new double[numericNumeric.size()];
        yArr = new double[numericNumeric.size()];
        for (int i = 0; i < numericNumeric.size(); i++) {
            xArr[i] = numericNumeric.get(i).getValueX();
            yArr[i] = numericNumeric.get(i).getValueY();
        }
        _xStats = new DescriptiveStatistics(xArr);
        _yStats = new DescriptiveStatistics(yArr);
        numericNumeric.sort(Comparator.comparing(NumericNumericDTO::getValueX));
    }

    public void addLinearRegression() {
        if (xArr == null) {
            _prepareData();
        }

        if (numericNumeric.isEmpty()) {
            return;
        }
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < numericNumeric.size(); i++) {
            regression.addData(numericNumeric.get(i).getValueX(), numericNumeric.get(i).getValueY());
        }

        NumericNumericDTO start = numericNumeric.get(0);
        NumericNumericDTO end = numericNumeric.get(numericNumeric.size() - 1);
        linearRegression = List.of(
                NumericNumericDTO.builder().valueX(start.getValueX()).valueY(regression.getIntercept() + regression.getSlope() * start.getValueX()).build(),
                NumericNumericDTO.builder().valueX(end.getValueX()).valueY(regression.getIntercept() + regression.getSlope() * end.getValueX()).build()
        );

    }

    public double addMeanX() {
        if (xArr == null) {
            _prepareData();
        }
        meanXValues = List.of(
                NumericNumericDTO.builder().valueX(_xStats.getMean()).valueY(_yStats.getMin()).build(),
                NumericNumericDTO.builder().valueX(_xStats.getMean()).valueY(_yStats.getMax()).build()
        );

        return _xStats.getMean();
    }

    public void addMeanY() {
        if (xArr == null) {
            _prepareData();
        }
        meanYValues = List.of(
                NumericNumericDTO.builder().valueX(_xStats.getMin()).valueY(_yStats.getMean()).build(),
                NumericNumericDTO.builder().valueX(_xStats.getMax()).valueY(_yStats.getMean()).build()
        );
    }

    public void predictOneYear(Map<Integer, Double> monthAverage) {
        if (numericNumeric.isEmpty()) {
            return;
        }
        numericNumeric.sort(Comparator.comparing(NumericNumericDTO::getValueX));

        prediction = new ArrayList<>();
        NumericNumericDTO mostRecentData = numericNumeric.get(numericNumeric.size() - 1);
        prediction.add(mostRecentData);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) mostRecentData.getValueX()), ZoneId.systemDefault());
        IntStream.range(0, 12).forEach(i -> {
            ZonedDateTime nextMonth = zdt.plusMonths(i + 1);
            Double valueY = monthAverage.get(nextMonth.getMonthValue());
            if (valueY != null) {
                prediction.add(NumericNumericDTO.builder()
                        .valueX(nextMonth.toInstant().toEpochMilli())
                        .valueY(valueY)
                        .build());
            }
        });
    }
}
