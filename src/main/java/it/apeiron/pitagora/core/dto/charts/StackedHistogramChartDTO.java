package it.apeiron.pitagora.core.dto.charts;

import it.apeiron.pitagora.core.entity.ModelField;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
public class StackedHistogramChartDTO extends ChartsDTO {

    private ChartType chartType = ChartType.STACKED_HISTOGRAM;
    private ModelField firstField;
    private ModelField secondField;
    private LinkedHashMap<String, LinkedHashMap<String, Double>> stackedValues;
    private Double total;
    private Double currentYearVariationComparedToPreviousYear;

    public StackedHistogramChartDTO(ModelField firstField, ModelField secondField, LinkedHashMap<String, LinkedHashMap<String, Double>> stackedValues) {
        this.firstField = firstField;
        this.secondField = secondField;
        this.stackedValues = stackedValues;
    }

    public void sortInnerByCount() {
        LinkedHashMap<String, LinkedHashMap<String, Double>> sorted = new LinkedHashMap<>();
        stackedValues.forEach((outerKey, stackValues) -> {
            List<HistogramCategoryDTO> inner = new ArrayList<>();
            stackValues.forEach((k, v) -> inner.add(new HistogramCategoryDTO(k, v)));
            inner.sort(new Comparator<HistogramCategoryDTO>() {
                @Override
                public int compare(HistogramCategoryDTO h1, HistogramCategoryDTO h2) {
                    return h1.getCount() < h2.getCount() ? -1 : 0;
                }
            });
            LinkedHashMap<String, Double> innerSorted = new LinkedHashMap<>();
            inner.forEach(cat -> innerSorted.put(cat.getCategory(), cat.getCount()));
            sorted.put(outerKey, innerSorted);
        });
        stackedValues = sorted;
    }

    public void sortOuterByStackTotalDesc() {
        List<StackedHelper> stacked = new ArrayList<>();
        stackedValues.forEach((k, v) -> {
            double stackCount = v.values().stream().reduce(Double::sum).orElse(0D);
            stacked.add(StackedHelper.builder().outerCategory(k).innerValues(v).stackCount(stackCount).build());
        });
        stacked.sort(new Comparator<StackedHelper>() {
            @Override
            public int compare(StackedHelper h1, StackedHelper h2) {
                return h1.stackCount > h2.stackCount ? -1 : 0;
            }
        });
        stackedValues.clear();
        stacked.forEach(stack -> stackedValues.put(stack.outerCategory, stack.innerValues));
    }

    public void sortByOuterCategory() {
        LinkedHashMap<String, LinkedHashMap<String, Double>> sorted = new LinkedHashMap<>();
        stackedValues.keySet().stream().sorted().forEachOrdered(key -> sorted.put(key, stackedValues.get(key)));
        stackedValues = sorted;
    }

    @Builder
    private static class StackedHelper {
        private String outerCategory;
        private LinkedHashMap<String, Double> innerValues;
        private Double stackCount;
    }

}
