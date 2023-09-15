package it.apeiron.pitagora.core.dto.charts;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.theorems.dire.dto.SiteDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.SameTimeIntervalCountDTO;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class HistogramChartDTO extends ChartsDTO {
    private ChartType chartType = ChartsDTO.ChartType.HISTOGRAM;
    private ModelField field;
    private List<HistogramCategoryDTO> categories;
    private double nullCount;
    private Double average;
    private Double total;
    private List<HistogramCategoryDTO> linearRegression;

    public HistogramChartDTO(ModelField field, List<HistogramCategoryDTO> categories) {
        this.field = field;
        this.categories = categories;
    }

    public HistogramChartDTO(List<HistogramCategoryDTO> categories) {
        this.categories = categories;
    }

    public HistogramChartDTO(Map<Integer, Double> categoriesValuesMap, Map<Integer, SameTimeIntervalCountDTO> sameTimeIntervalExpenses) {
        categories = new ArrayList<>();
        categoriesValuesMap.forEach((k, v) ->
                categories.add(new HistogramCategoryDTO(k.toString(), v, sameTimeIntervalExpenses.get(k).getVariationPercentage()))
        );
        categories = categories.stream().sorted(Comparator.comparing(HistogramCategoryDTO::getCategory)).collect(Collectors.toList());
    }

    public static HistogramChartDTO fromSiteAndConsumption(Map<String, Double> categoriesValuesMap) {
        List<HistogramCategoryDTO> categories = new ArrayList<>();
        categoriesValuesMap.forEach((k, v) ->
                categories.add(new HistogramCategoryDTO(k, v))
        );

        List<HistogramCategoryDTO> finalCategories = categories.stream().sorted(Comparator.comparing(HistogramCategoryDTO::getCount).reversed()).collect(Collectors.toList());

        return HistogramChartDTO.builder()
                .chartType(ChartType.HISTOGRAM)
                .categories(finalCategories)
                .build();
    }

    public static HistogramChartDTO fromSites(List<SiteDTO> sites) {
        return HistogramChartDTO.builder()
                .chartType(ChartType.HISTOGRAM)
                .categories(sites.stream()
                        .map(site -> new HistogramCategoryDTO(site.getSede(), (double) site.getMetadata())).sorted(Comparator.comparing(HistogramCategoryDTO::getCount).reversed()).collect(Collectors.toList()))
                .build();
    }

    public void calcAverage() {
        this.average = this.categories.stream().mapToDouble(HistogramCategoryDTO::getCount).average().orElse(0.0);
    }

    public void sortByCountDesc() {
        categories.sort(new Comparator<HistogramCategoryDTO>() {
            @Override
            public int compare(HistogramCategoryDTO h1, HistogramCategoryDTO h2) {
                return h1.getCount() < h2.getCount() ? -1 : 0;
            }
        });
    }

    public void sortByCategoryAsc() {
        categories.sort(new Comparator<HistogramCategoryDTO>() {
            @Override
            public int compare(HistogramCategoryDTO h1, HistogramCategoryDTO h2) {
                return h1.getCategory().compareTo(h2.getCategory());
            }
        });
    }
}
