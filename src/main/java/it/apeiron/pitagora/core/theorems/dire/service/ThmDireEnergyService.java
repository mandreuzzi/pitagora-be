package it.apeiron.pitagora.core.theorems.dire.service;

import static it.apeiron.pitagora.core.theorems.dire.DireQueryUtils.MILLION_CONVERTER;
import static it.apeiron.pitagora.core.theorems.dire.DireQueryUtils.THOUSANDS_CONVERTER;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.ArrayElemAt.arrayOf;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.year;

import it.apeiron.pitagora.core.dto.charts.HistogramChartDTO;
import it.apeiron.pitagora.core.dto.charts.LinearChartDTO;
import it.apeiron.pitagora.core.dto.charts.NumericNumericDTO;
import it.apeiron.pitagora.core.dto.charts.StackedHistogramChartDTO;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.DireUserPreferences;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import it.apeiron.pitagora.core.theorems.dire.dto.EnergyExpensesDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.MaintenanceRequestDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SiteDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.TotalEnergyExpensesDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.EnergyTabDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.SumFieldByYearAndMonthDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmDireEnergyService {



    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public EnergyTabDTO getElectricityTab(MaintenanceRequestDTO dto) {
        return _getEnergyTab(
                dto,
                ThmDireEntity.ENERGY_ELECTRICITY_CONSUMPTION,
                "energia",
                ThmDireEntity.ENERGY_ELECTRICITY_EXPENSES,
                "costoII",
                "periodo"
        );
    }

    public EnergyTabDTO getGasTab(MaintenanceRequestDTO dto) {
        return _getEnergyTab(
                dto,
                ThmDireEntity.ENERGY_GAS_CONSUMPTION,
                "quantitaGas",
                ThmDireEntity.ENERGY_GAS_CONSUMPTION,
                "ricaviVendita",
                "data"
        );
    }

    private EnergyTabDTO _getEnergyTab(MaintenanceRequestDTO dto, ThmDireEntity consumptionEntity, String consumptionField,
                                       ThmDireEntity expensesEntity, String expensesField, String expensesTimeField) {

        List<AggregationOperation> consumptionAddressesYearsFilter = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(dto, List.of("data"));

        List<AggregationOperation> expensesAddressesYearsFilter = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(dto, List.of(expensesTimeField));
        List<SiteDTO> sitesWithSpecificConsumption = _getConsumptionBySite(consumptionAddressesYearsFilter, consumptionField, consumptionEntity);

        SumFieldByYearAndMonthDTO consumptionByYearAndMonth = sp.direQueryUtils.sumFieldByYearAndMonth(consumptionAddressesYearsFilter, consumptionField, "data", consumptionEntity);
        StackedHistogramChartDTO consumptionByMonth = new StackedHistogramChartDTO();
        consumptionByMonth.setFirstField(ModelField.builder().type(FieldType.STRING).build());
        consumptionByMonth.setSecondField(ModelField.builder().type(FieldType.STRING).build());
        consumptionByYearAndMonth.getStackedValues().keySet().forEach(month -> {
            consumptionByYearAndMonth.getStackedValues().get(month).forEach((year, consumption) -> consumptionByYearAndMonth.getStackedValues().get(month).put(year, consumption / THOUSANDS_CONVERTER));
        });
        consumptionByMonth.setStackedValues(consumptionByYearAndMonth.getStackedValues());

        Map<Integer, List<NumericNumericDTO>> consumptionByYearData = consumptionByYearAndMonth.getByYearData();
        Map<Integer, Double> consumptionByYear = sp.direQueryUtils.sumFieldByYear(consumptionByYearData);
        consumptionByYear.keySet().forEach(key -> consumptionByYear.put(key, consumptionByYear.get(key) / MILLION_CONVERTER));

        SumFieldByYearAndMonthDTO expensesByYearAndMonth = sp.direQueryUtils.sumFieldByYearAndMonth(expensesAddressesYearsFilter, expensesField, expensesTimeField, expensesEntity);
        StackedHistogramChartDTO expensesByMonth = new StackedHistogramChartDTO();
        expensesByMonth.setFirstField(ModelField.builder().type(FieldType.STRING).build());
        expensesByMonth.setSecondField(ModelField.builder().type(FieldType.STRING).build());
        expensesByYearAndMonth.getStackedValues().keySet().forEach(month -> {
            expensesByYearAndMonth.getStackedValues().get(month).forEach((year, expenses) -> expensesByYearAndMonth.getStackedValues().get(month).put(year, expenses / THOUSANDS_CONVERTER));
        });
        expensesByMonth.setStackedValues(expensesByYearAndMonth.getStackedValues());

        Map<Integer, List<NumericNumericDTO>> expensesByYearData = expensesByYearAndMonth.getByYearData();
        Map<Integer, Double> expensesByYear = sp.direQueryUtils.sumFieldByYear(expensesByYearData);
        expensesByYear.keySet().forEach(key -> expensesByYear.put(key, expensesByYear.get(key) / MILLION_CONVERTER));

        TotalEnergyExpensesDTO totalEnergyExpenses = _getTotalEnergyExpenses(expensesEntity, expensesField, expensesTimeField);

        LinearChartDTO expensesHistoryChart = getExpensesHistory(expensesByYearAndMonth);

        int lastYear = dto.getYears().get(dto.getYears().size() - 1) + 1;
        MaintenanceRequestDTO newDTO = new MaintenanceRequestDTO(List.of(lastYear), dto.getAddresses());
        List<AggregationOperation> expensesAddressesYearsFilterLastYear = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(newDTO, List.of(expensesTimeField));
        SumFieldByYearAndMonthDTO expensesByYearAndMonthLastYearWithData = sp.direQueryUtils.sumFieldByYearAndMonth(expensesAddressesYearsFilterLastYear, expensesField, expensesTimeField, expensesEntity);

        if (expensesByYearAndMonthLastYearWithData.getByYearData().keySet().size() > 0) {
            expensesHistoryChart.getNumericNumeric().addAll(getExpensesHistory(expensesByYearAndMonthLastYearWithData).getNumericNumeric());
        }

        DireUserPreferences preferences = (DireUserPreferences) sp.jwtService.getUserTheoremPreferences(Theorem.DIRE);
        EnergyTabDTO tab;
        if (ThmDireEntity.ENERGY_ELECTRICITY_CONSUMPTION.equals(consumptionEntity)) {
            tab = preferences.getElectricityTab();
        } else {
            tab = preferences.getGasTab();
        }

        tab.getConsumptionBySite().setContent(HistogramChartDTO.fromSites(sitesWithSpecificConsumption));
        tab.getConsumptionByMonth().setContent(consumptionByMonth);
        tab.getConsumptionByYears().setContent(new HistogramChartDTO(consumptionByYear, consumptionByYearAndMonth.getSameTimeIntervalCount()));
        tab.getExpensesByMonth().setContent(expensesByMonth);
        tab.getExpensesByYears().setContent(new HistogramChartDTO(expensesByYear, expensesByYearAndMonth.getSameTimeIntervalCount()));
        tab.getExpensesHistory().setContent(expensesHistoryChart);
        tab.getTotalEnergyExpenses().setContent(totalEnergyExpenses);
        tab.setConsumptionBySiteOnMap(SiteDTO.calcMarkerRadiusViaMetadata(sitesWithSpecificConsumption));

        return tab;
    }

    private TotalEnergyExpensesDTO _getTotalEnergyExpenses(ThmDireEntity expensesEntity, String expensesField, String expensesTimeField) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>();
        aggregationsPipeline.add(Aggregation.project()
                .andExpression("data." + expensesField).as("costoII")
                .andExpression("data." + expensesTimeField).as("anno")

        );

        aggregationsPipeline.add(Aggregation.addFields()
                .addField("anno").withValue(year(toDate("$anno"))
                ).build());

        int currentYear = LocalDate.now().getYear();
        aggregationsPipeline.add(Aggregation.match(Criteria.where("anno").is(currentYear)
        ));

        TotalEnergyExpensesDTO total = new TotalEnergyExpensesDTO(0);

        sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                expensesEntity.pitagoraName(), EnergyExpensesDTO.class
        ).getMappedResults().forEach(obj -> {
            total.setTotalExpensesII(total.getTotalExpensesII() + obj.getCostoII());
        });
        if (expensesEntity.equals(ThmDireEntity.ENERGY_GAS_CONSUMPTION)) {
            total.setTotalExpensesII(total.getTotalExpensesII() * 1.22);
        }

        total.setTotalExpensesII(total.getTotalExpensesII() / MILLION_CONVERTER);
        return total;
    }


    private List<SiteDTO> _getConsumptionBySite(List<AggregationOperation> yearsAndAddressesFilters, String consumptionField, ThmDireEntity entity) {

        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        aggregationsPipeline.add(Aggregation.project()
                .andExpression("data.sede").as("sede")
                .andExpression("data." + consumptionField).as("consumption")
        );
        aggregationsPipeline.add(Aggregation.group("sede").sum("consumption").as("value"));

        aggregationsPipeline.add(Aggregation.lookup(ThmDireEntity.SITE.pitagoraName(), "_id", "data.sede", "aggregations"));

        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(
                Criteria.where("aggregations.data.surface").ne(null),
                Criteria.where("aggregations.data.surface").gt(0))
        ));

        aggregationsPipeline.add(Aggregation.project()
                .and("value").as("value")
                .and(arrayOf("aggregations.data.description").elementAt(0)).as("description")
                .and(arrayOf("aggregations.data.latitude").elementAt(0)).as("latitude")
                .and(arrayOf("aggregations.data.longitude").elementAt(0)).as("longitude")
                .and(arrayOf("aggregations.data.surface").elementAt(0)).as("surface")
        );

        aggregationsPipeline.add(Aggregation.project()
                .and("_id").as("sede")
                .and("description").as("description")
                .and("latitude").as("latitude")
                .and("longitude").as("longitude")
                .and("value").divide("surface").as("metadata")
        );

        return sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        entity.pitagoraName(), SiteDTO.class)
                .getMappedResults();

    }


    public LinearChartDTO getExpensesHistory(SumFieldByYearAndMonthDTO groupedByYearAndMonth) {
        LinearChartDTO chart = new LinearChartDTO();
        chart.setValueFieldX(ModelField.builder().description("Data").type(FieldType.TIMESTAMP).build());
        chart.setValueFieldY(ModelField.builder().description("Costi").type(FieldType.DOUBLE).build());
        chart.setNumericNumeric(new ArrayList<>());
        groupedByYearAndMonth.getByYearData().forEach((year, months) ->
                months.forEach(monthWithoutYear -> {
                            ZonedDateTime currentMonth = ZonedDateTime.ofInstant(Instant.ofEpochMilli((long) monthWithoutYear.getValueX()), ZoneId.systemDefault());
                            chart.getNumericNumeric().add(NumericNumericDTO.builder()
                                    .valueX(ZonedDateTime.of(year, currentMonth.getMonthValue(), 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli())
                                    .valueY(monthWithoutYear.getValueY() / THOUSANDS_CONVERTER).build());
                        }
                )
        );

        groupedByYearAndMonth.getMonthAverage().keySet().forEach(key -> groupedByYearAndMonth.getMonthAverage().put(key, groupedByYearAndMonth.getMonthAverage().get(key) / THOUSANDS_CONVERTER));//convert into milion

        chart.predictOneYear(groupedByYearAndMonth.getMonthAverage());
        return chart;
    }

}
