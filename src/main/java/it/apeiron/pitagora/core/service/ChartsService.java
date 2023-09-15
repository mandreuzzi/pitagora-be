package it.apeiron.pitagora.core.service;

import it.apeiron.pitagora.core.dto.charts.CategoryNumericDTO;
import it.apeiron.pitagora.core.dto.charts.ChartsDTO;
import it.apeiron.pitagora.core.dto.charts.FieldDataDTO;
import it.apeiron.pitagora.core.dto.charts.HistogramCategoryDTO;
import it.apeiron.pitagora.core.dto.charts.HistogramChartDTO;
import it.apeiron.pitagora.core.dto.charts.LinearChartDTO;
import it.apeiron.pitagora.core.dto.charts.NumericNumericDTO;
import it.apeiron.pitagora.core.dto.charts.RequestChartDTO;
import it.apeiron.pitagora.core.dto.charts.SingleNumericChartDTO;
import it.apeiron.pitagora.core.dto.charts.StackedHistogramChartDTO;
import it.apeiron.pitagora.core.dto.charts.WhiskerChartDTO;
import it.apeiron.pitagora.core.entity.Widget.ChartWidget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChartsService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public ChartsDTO getChart(RequestChartDTO requestChart) {
        Map<String, FieldDataDTO> map = sp.dataRecordsService.getDataRecordsAsColumnsByDatasetId(requestChart.getQuery(), requestChart.getColumnNameX(), requestChart.getColumnNameY());
        return getChart(map.get(requestChart.getColumnNameX()), map.get(requestChart.getColumnNameY()));
    }

    public ChartsDTO getChart(ChartWidget widget) {
        return getChart(RequestChartDTO.builder()
                .query(widget.getQuery())
                .columnNameX(widget.getColumnNameX())
                .columnNameY(widget.getColumnNameY())
                .build());
    }

    private ChartsDTO getChart(@NonNull FieldDataDTO fieldDataX, FieldDataDTO fieldDataY) {

        if (fieldDataY != null) {

            boolean numX = fieldDataX.isNumerical();
            boolean numY = fieldDataY.isNumerical();
            boolean timeX = fieldDataX.isTimestamp();
            boolean timeY = fieldDataY.isTimestamp();
            boolean catX = fieldDataX.isCategorical();
            boolean catY = fieldDataY.isCategorical();

            if (numX && numY) {
                return _getLinearChart(fieldDataX, fieldDataY);
            } else if (((catX || timeX) && catY) || (catX && (catY || timeY))) {
                return getStackedChart(fieldDataX, fieldDataY);
            } else if (catX && numY) {
                return _getWhiskerChart(fieldDataX, fieldDataY);
            } else if (numX && catY) {
                return _getWhiskerChart(fieldDataY, fieldDataX);
            }

        } else if (fieldDataX.isNumerical()) {
            return _getSingleNumeric(fieldDataX);
        } else if (fieldDataX.isCategorical()) {
            return getHistogramChart(fieldDataX);
        }
        return null;
    }

    private SingleNumericChartDTO _getSingleNumeric(FieldDataDTO fieldDataX) {
        List<Double> numbers = new ArrayList<>();
        for (int i = 0; i < fieldDataX.getValues().size(); i++) {
            if (fieldDataX.getValues().get(i) != null) {
                numbers.add(Double.parseDouble(fieldDataX.getValues().get(i).toString()));
            }
        }

        return new SingleNumericChartDTO(fieldDataX.getField(), numbers);
    }

    private ChartsDTO _getLinearChart(FieldDataDTO fieldDataX, FieldDataDTO fieldDataY) {
        List<NumericNumericDTO> numericNumericList = new ArrayList<>();

        for (int i = 0; i < fieldDataX.getValues().size(); i++) {
            if (fieldDataX.getValues().get(i) != null && fieldDataY.getValues().get(i) != null) {
                NumericNumericDTO numericNumeric = NumericNumericDTO.builder().valueX(Double.parseDouble(fieldDataX.getValues().get(i).toString())).valueY(Double.parseDouble(fieldDataY.getValues().get(i).toString())).build();
                numericNumericList.add(numericNumeric);
            }
        }

        return new LinearChartDTO(fieldDataX.getField(), fieldDataY.getField(), numericNumericList);
    }

    public StackedHistogramChartDTO getStackedChart(FieldDataDTO fieldDataX, FieldDataDTO fieldDataY) {

        LinkedHashMap<String, LinkedHashMap<String, Double>> map = new LinkedHashMap<>();

        for (int i = 0; i < fieldDataX.getValues().size(); i++) {
            String currentValueX = "";
            String currentValueY = "";

            try {
                String cast = (String) fieldDataX.getValues().get(i);
                if (StringUtils.isNotEmpty(cast)) {
                    currentValueX = fieldDataX.getValues().get(i).toString();
                }
            } catch (ClassCastException e) {
                currentValueX = fieldDataX.getValues().get(i).toString();
            }

            try {
                String cast = (String) fieldDataY.getValues().get(i);
                if (StringUtils.isNotEmpty(cast)) {
                    currentValueY = fieldDataY.getValues().get(i).toString();
                }
            } catch (ClassCastException e) {
                currentValueY = fieldDataY.getValues().get(i).toString();
            }

            if (!map.containsKey(currentValueX)) {
                map.put(currentValueX, new LinkedHashMap<>());
            }
            if (!map.get(currentValueX).containsKey(currentValueY)) {
                map.get(currentValueX).put(currentValueY, 0.0);
            }
            double currentCount = map.get(currentValueX).get(currentValueY);
            map.get(currentValueX).put(currentValueY, ++currentCount);
        }

        return new StackedHistogramChartDTO(fieldDataX.getField(), fieldDataY.getField(), map);
    }

    public HistogramChartDTO getHistogramChart(FieldDataDTO fieldDataX) {

        List<HistogramCategoryDTO> countCategories = new ArrayList<>();
        AtomicReference<Integer> nullCategory = new AtomicReference<>();
        fieldDataX.getValues().forEach(ele -> {
            if (ele == null || StringUtils.isEmpty( ele.toString())) {
                if (nullCategory.get() == null) {
                    nullCategory.set((int) fieldDataX.getValues().stream().filter(value -> StringUtils.isEmpty((String) value)).count());
                }
                return;
            }
            if (countCategories.stream().anyMatch(subEle -> ele.toString().equals((subEle).getCategory()))) {
                return;
            }
            HistogramCategoryDTO countCategory = new HistogramCategoryDTO();
            countCategory.setCategory(ele.toString());
            countCategory.setCount((int) fieldDataX.getValues().stream().filter(ele::equals).count());
            countCategories.add(countCategory);
        });
        List<HistogramCategoryDTO> sortedCategories = countCategories.stream()
                .sorted(Comparator.comparing(HistogramCategoryDTO::getCount).reversed()).collect(Collectors.toList());

        HistogramChartDTO chart = new HistogramChartDTO(fieldDataX.getField(), sortedCategories);

        if (nullCategory.get() != null) {
            chart.setNullCount(nullCategory.get());
        }
        return chart;
    }

    private WhiskerChartDTO _getWhiskerChart(FieldDataDTO fieldDataX, FieldDataDTO fieldDataY) {

        List<CategoryNumericDTO> categoriesValues = new ArrayList<>();

        for (int i = 0; i < fieldDataX.getValues().size(); i++) {
            CategoryNumericDTO categoryNumeric;
            if (fieldDataY.getValues().get(i) == null) {
                continue;
            } else if (fieldDataX.getValues().get(i) == null) {
                categoryNumeric = CategoryNumericDTO.builder()
                        .category("")
                        .value(Double.parseDouble(fieldDataY.getValues().get(i).toString())).build();
            } else {
                categoryNumeric = CategoryNumericDTO.builder()
                        .category(fieldDataX.getValues().get(i).toString())
                        .value(Double.parseDouble(fieldDataY.getValues().get(i).toString())).build();
            }

            categoriesValues.add(categoryNumeric);
        }

        return new WhiskerChartDTO(fieldDataX.getField(), fieldDataY.getField(), categoriesValues);
    }
}
