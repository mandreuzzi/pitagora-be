package it.apeiron.pitagora.core.theorems.dire.service;

import it.apeiron.pitagora.core.dto.charts.*;
import it.apeiron.pitagora.core.dto.charts.ChartsDTO.ChartType;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.DireUserPreferences;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import it.apeiron.pitagora.core.theorems.dire.dto.AnnualScopesDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.MaintenanceRequestDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SiteDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SustainabilityTabDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.SustainabilityConsumptionSiteAndYearDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.ValueGroupByDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static it.apeiron.pitagora.core.theorems.dire.ThmDireEntity.ENERGY_ELECTRICITY_CONSUMPTION;
import static it.apeiron.pitagora.core.theorems.dire.ThmDireEntity.SITE;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Month.month;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.year;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmDireSustainabilityService {

    private final static Double SCOPE_ONE_CONSTANT = 0.3524;
    private final static Double SCOPE_TWO_CONSTANT = 0.32678;
    private final static Double ARCH_2030_CONSTANT = 74.8;

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public SustainabilityTabDTO getSustainabilityTab(MaintenanceRequestDTO dto) {

        Map<String, Double> allSitesSurface = new HashMap<>();
        sp.mongoTemplateData.findAll(PitagoraData.class, SITE.pitagoraName()).stream()
                .filter(data -> data.getData().get("surface") != null)
                .forEach(data -> allSitesSurface.put((String) data.getData().get("sede"), (double) data.getData().get("surface")));

        List<AggregationOperation> yearsAndAddressesFilters = sp.direQueryUtils.buildCommonYearsAndAddressesFilters(dto, List.of("data"));

        List<SustainabilityConsumptionSiteAndYearDTO> annualConsumption = new ArrayList<>();
        annualConsumption.addAll(_loadAnnualConsumption(ThmDireEntity.ENERGY_GAS_CONSUMPTION, "quantitaGas", yearsAndAddressesFilters));
        annualConsumption.addAll(_loadAnnualConsumption(ThmDireEntity.ENERGY_ELECTRICITY_CONSUMPTION, "energia", yearsAndAddressesFilters));

        List<SustainabilityConsumptionSiteAndYearDTO> mergedAnnualConsumption = _mergeElectricalAndGasConsumptionOfSameSite(annualConsumption);

        List<ValueGroupByDTO> monthConsumption = new ArrayList<>();
        monthConsumption.addAll(_loadMonthConsumption(ThmDireEntity.ENERGY_GAS_CONSUMPTION, "quantitaGas", yearsAndAddressesFilters));
        monthConsumption.addAll(_loadMonthConsumption(ThmDireEntity.ENERGY_ELECTRICITY_CONSUMPTION, "energia", yearsAndAddressesFilters));

        Map<String, Map<Integer, Double>> specificConsumptionByYear = _getSpecificConsumptionByYear(mergedAnnualConsumption, allSitesSurface);
        Map<String, Double> siteConsumption = new HashMap<>();
        specificConsumptionByYear.forEach((site, values) -> siteConsumption.put(site, (values.values().stream().mapToDouble(Double::doubleValue).sum()) / values.keySet().size()));

        SustainabilityTabDTO tab = ((DireUserPreferences) sp.jwtService.getUserTheoremPreferences(Theorem.DIRE)).getSustainabilityTab();
        tab.getScopes().setContent(_getScopes(annualConsumption, monthConsumption));
        tab.getSpecificConsumptionBySite().setContent(HistogramChartDTO.fromSiteAndConsumption(siteConsumption));
        tab.getObjectiveAnalysis().setContent(_getObjectiveAnalysis(specificConsumptionByYear));
        tab.setSpecificConsumptionBySiteOnMap(_buildConsumptionBySiteOnMap(siteConsumption));

        return tab;
    }

    private List<SustainabilityConsumptionSiteAndYearDTO> _mergeElectricalAndGasConsumptionOfSameSite(
            List<SustainabilityConsumptionSiteAndYearDTO> annualConsumption) {
        Map<String, Map<Integer, Double>> merged = new HashMap<>();
        annualConsumption.forEach(record -> {
            if (!merged.containsKey(record.getSite())) {
                merged.put(record.getSite(), new HashMap<>());
            }

            if (!merged.get((record.getSite())).containsKey(record.getYear())) {
                merged.get(record.getSite()).put(record.getYear(), 0D);
            }
            double current = merged.get(record.getSite()).get(record.getYear());
            current += record.getConsumption();
            merged.get(record.getSite()).put(record.getYear(), current);
        });
        List<SustainabilityConsumptionSiteAndYearDTO> merdedRes = new ArrayList<>();
        merged.forEach((site, yearsValues) ->
                yearsValues.forEach((year, consumption) -> {
                    if (consumption != 0){
                        merdedRes.add(SustainabilityConsumptionSiteAndYearDTO.builder()
                                .site(site)
                                .year(year)
                                .consumption(consumption)
                                .build());
                    }
                })
        );
        return merdedRes;
    }

    private Map<String, Map<Integer, Double>> _getSpecificConsumptionByYear(List<SustainabilityConsumptionSiteAndYearDTO> list, Map<String, Double> allSitesSurface) {

        Map<String, Map<Integer, Double>> results = new HashMap<>();
        list.forEach(record -> {
            if (!results.containsKey(record.getSite())) {
                results.put(record.getSite(), new HashMap<>());
            }

            if (!results.get((record.getSite())).containsKey(record.getYear())) {
                results.get(record.getSite()).put(record.getYear(), 0D);
            }

            if (allSitesSurface.get(record.getSite()) == null) {
                return;
            }
            double surface = allSitesSurface.get(record.getSite());
            double consumption = record.getConsumption();
            double previousValue = results.get(record.getSite()).get(record.getYear());
            results.get(record.getSite()).put(record.getYear(), previousValue + consumption / surface);

        });

        return results;
    }

    private HistogramChartDTO _getObjectiveAnalysis(Map<String, Map<Integer, Double>> specificConsumptionByYear) {

        Map<Integer, List<Double>> consumptionsListByYear = new HashMap<>();

        specificConsumptionByYear.forEach((site, yearsValues) ->
                yearsValues.forEach((year, consumption) -> {
                    if (!consumptionsListByYear.containsKey(year)) {
                        consumptionsListByYear.put(year, new ArrayList<>());
                    }
                    consumptionsListByYear.get(year).add(consumption);
                })
        );

        List<HistogramCategoryDTO> categories = new ArrayList<>();
        consumptionsListByYear.forEach((year, sitesValues) -> {
            double consumption = sitesValues.stream().mapToDouble(value -> value).sum() / sitesValues.size();
            categories.add(new HistogramCategoryDTO(Integer.toString(year), consumption));
        });


        LinearChartDTO chart = LinearChartDTO.builder()
                .numericNumeric(categories.stream().map(category -> new NumericNumericDTO(Integer.parseInt(category.getCategory()), category.getCount())).sorted(Comparator.comparingDouble(NumericNumericDTO::getValueX)).collect(Collectors.toList()))
                .valueFieldX(ModelField.builder().description("Data").type(FieldType.TIMESTAMP).build())
                .valueFieldY(ModelField.builder().description("Media").type(FieldType.DOUBLE).build())
                .chartType(ChartsDTO.ChartType.LINEAR)
                .build();

        chart.addLinearRegression();

        HistogramChartDTO dto = HistogramChartDTO.builder()
                .chartType(ChartType.HISTOGRAM)
                .categories(categories)
                .field(ModelField.builder().description("Data").type(FieldType.TIMESTAMP).build())

                .build();
        dto.sortByCategoryAsc();
        dto.setAverage(ARCH_2030_CONSTANT);
        if (chart.getLinearRegression() != null) {
            dto.setLinearRegression(chart.getLinearRegression().stream()
                    .map(numeric -> new HistogramCategoryDTO(String.valueOf((int) numeric.getValueX()), numeric.getValueY())).collect(Collectors.toList()));
        }

        return dto;
    }


    private AnnualScopesDTO _getScopes(List<SustainabilityConsumptionSiteAndYearDTO> annualConsumption, List<ValueGroupByDTO> monthConsumption) {
        return AnnualScopesDTO.builder()
                .firstAnnualScope(_getAnnualScope(annualConsumption, SCOPE_ONE_CONSTANT))
                .secondAnnualScope(_getAnnualScope(annualConsumption, SCOPE_TWO_CONSTANT))
                .firstMonthScope(_getMonthScope(monthConsumption, SCOPE_ONE_CONSTANT))
                .secondMonthScope(_getMonthScope(monthConsumption, SCOPE_TWO_CONSTANT))
                .build();
    }

    private Double _getAnnualScope(List<SustainabilityConsumptionSiteAndYearDTO> list, Double constants) {

        Map<Integer, Double> results = new HashMap<>();

        list.forEach(record -> {
            if (!results.containsKey(record.getYear())) {
                results.put(record.getYear(), 0D);
            }
            results.put(record.getYear(), results.get(record.getYear()) + (record.getConsumption() * constants));
        });

        AtomicReference<Double> sum = new AtomicReference<>(0D);
        int years = results.keySet().size();
        results.values().forEach(value -> sum.set(sum.get() + value));

        return sum.get() / years;

    }

    private Double _getMonthScope(List<ValueGroupByDTO> list, Double constants) {

        AtomicReference<Double> sum = new AtomicReference<>(0D);
        list.forEach(month -> sum.set(sum.get() + (month.getValue() * constants)));

        return sum.get() / list.size();
    }

    private List<SustainabilityConsumptionSiteAndYearDTO> _loadAnnualConsumption(ThmDireEntity entity, String consumptionField, List<AggregationOperation> yearsAndAddressesFilters) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        ProjectionOperation projection = Aggregation.project()
                .andExpression("data.data").as("date")
                .andExpression("data.sede").as("site")
                .andExpression("data." + consumptionField).as("consumption");
        if (ThmDireEntity.ENERGY_GAS_CONSUMPTION.equals(entity)) {
            projection = projection.andExpression("data.coefficenteDiConversioneInKwh").as("coefficente");
        }
        aggregationsPipeline.add(projection);

        aggregationsPipeline.add(Aggregation.addFields()
                .addField("year").withValue(year(toDate("$date"))
                ).build());

        if (ThmDireEntity.ENERGY_GAS_CONSUMPTION.equals(entity)) {
            aggregationsPipeline.add(Aggregation.project()
                    .and("consumption").multiply("coefficente").as("consumption")
                    .and("year").as("year")
                    .and("site").as("site")
            );
        }

        aggregationsPipeline.add(Aggregation.group("site", "year").sum("consumption").as("consumption"));
        aggregationsPipeline.add(Aggregation.project()
                .and("_id.site").as("site")
                .and("_id.year").as("year")
                .and("consumption").as("consumption")
        );
        return sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                entity.pitagoraName(), SustainabilityConsumptionSiteAndYearDTO.class
        ).getMappedResults();
    }

    private List<ValueGroupByDTO> _loadMonthConsumption(ThmDireEntity entity, String consumptionField, List<AggregationOperation> yearsAndAddressesFilters) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        ProjectionOperation projection = Aggregation.project()
                .andExpression("data.data").as("date")
                .andExpression("data." + consumptionField).as("consumption");
        if (ThmDireEntity.ENERGY_GAS_CONSUMPTION.equals(entity)) {
            projection = projection.andExpression("data.coefficenteDiConversioneInKwh").as("coefficente");
        }
        aggregationsPipeline.add(projection);

        aggregationsPipeline.add(Aggregation.addFields()
                .addField("year").withValue(year(toDate("$date")))
                .addField("month").withValue(month(toDate("$date")))
                .build());

        if (ThmDireEntity.ENERGY_GAS_CONSUMPTION.equals(entity)) {
            aggregationsPipeline.add(Aggregation.project()
                    .and("consumption").multiply("coefficente").as("consumption")
                    .and("year").as("year")
                    .and("month").as("month")
            );
        }

        aggregationsPipeline.add(Aggregation.group("month", "year").sum("consumption").as("value"));
        return sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                entity.pitagoraName(), ValueGroupByDTO.class
        ).getMappedResults();
    }

    private List<SiteDTO> _buildConsumptionBySiteOnMap(Map<String, Double> consumptionBySite) {
        List<SiteDTO> partialResults = sp.direQueryUtils.getAllSites(List.of(ThmDireEntity.ENERGY_GAS_CONSUMPTION, ENERGY_ELECTRICITY_CONSUMPTION), new ArrayList<>(consumptionBySite.keySet())).stream()
                .map(record -> new SiteDTO(record.getData())).collect(Collectors.toList());

        List<SiteDTO> results = new ArrayList<>();
        partialResults.forEach(sede -> {
            Double consumption = consumptionBySite.get(sede.getSede());
            if(consumption != null){
                sede.setMetadata(consumption);
                results.add(sede);
            }
        });

        return SiteDTO.calcMarkerRadiusViaMetadata(results);
    }
}
