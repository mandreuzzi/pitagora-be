package it.apeiron.pitagora.core.theorems.dire;

import it.apeiron.pitagora.core.dto.charts.NumericNumericDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.dto.MaintenanceRequestDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.CategoryValueDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.SameTimeIntervalCountDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.SumFieldByYearAndMonthDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.ValueGroupByDTO;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation.AddFieldsOperationBuilder;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Month.month;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.year;

@Service
public class DireQueryUtils {

    private ServiceProvider sp;

    public final static int MILLION_CONVERTER = 1_000_000;
    public final static int THOUSANDS_CONVERTER = 1_000;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public List<PitagoraData> getAllSites(List<ThmDireEntity> datasetsToSearchIn, List<String> sedeToMatch) {
        Set<String> addresses = new HashSet<>();
        datasetsToSearchIn.forEach(dataset ->
                addresses.addAll(categoriesWithCountByField(dataset, "sede", Collections.emptyList(), true).stream()
                        .filter(cat -> sedeToMatch.isEmpty() || sedeToMatch.contains(cat.getCategory()))
                        .map(CategoryValueDTO::getCategory)
                        .collect(Collectors.toList())));

        return sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(
                                Aggregation.match(new Criteria().andOperator(Criteria.where("data.sede").in(addresses)))), ThmDireEntity.SITE.pitagoraName(),
                        PitagoraData.class).getMappedResults();

    }

    public List<CategoryValueDTO> categoriesWithCountByField(ThmDireEntity dataset, String field, List<AggregationOperation> yearsAndAddressesFilters, boolean excludeNull) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);
        if (excludeNull) {
            aggregationsPipeline.add(Aggregation.match(Criteria.where("data." + field).ne(null)));
        }
        aggregationsPipeline.add(Aggregation.group("data." + field).count().as("count"));
        aggregationsPipeline.add(Aggregation.project()
                .andExpression("_id").as("category")
                .andExpression("count").as("value")
        );
        return sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        dataset.pitagoraName(), CategoryValueDTO.class)
                .getMappedResults();
    }

    public SumFieldByYearAndMonthDTO sumFieldByYearAndMonth(List<AggregationOperation> yearsAndAddressesFilters, String fieldToBeSummed, String timeField, ThmDireEntity entity) {
        List<ValueGroupByDTO> byYearAndMonth = _queryFieldAndSumByYearAndMonth(yearsAndAddressesFilters, fieldToBeSummed, timeField, entity);

        Map<Integer, List<NumericNumericDTO>> dataByYear = new HashMap<>();
        Map<Integer, List<Double>> valuesByMonth = new HashMap<>();
        LinkedHashMap<Integer, LinkedHashMap<String, Double>> stackedValues = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            stackedValues.put(i, new LinkedHashMap<>());
        }

        int lastMonthToEvaluate = LocalDateTime.now().getMonthValue();

        Map<Integer, SameTimeIntervalCountDTO> sameTimeIntervalExpenses = new HashMap<>();
        byYearAndMonth.forEach(ym -> {
            int year = (int) ym.get_id().get("year");
            if (!dataByYear.containsKey(year)) {
                dataByYear.put(year, new ArrayList<>());
            }

            if (!sameTimeIntervalExpenses.containsKey(year)) {
                sameTimeIntervalExpenses.put(year, new SameTimeIntervalCountDTO());
            }

            int month = (int) ym.get_id().get("month"); //Mese come intero 1,2,3,4....
            if (month < lastMonthToEvaluate) {
                SameTimeIntervalCountDTO previousValue = sameTimeIntervalExpenses.get(year);
                previousValue.addExpense(ym.getValue());
                sameTimeIntervalExpenses.put(year, previousValue);
            }

            if (!valuesByMonth.containsKey(month)) {
                valuesByMonth.put(month, new ArrayList<>());
            }
            valuesByMonth.get(month).add(ym.getValue());

            stackedValues.get(month).put(String.valueOf(year), ym.getValue());


            dataByYear.get(year).add(NumericNumericDTO.builder()
                    .valueX(LocalDateTime.of(2000, month, 1, 0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())//Convertire quel month in stringa
                    .valueY(ym.getValue())
                    .build());
        });
        Map<Integer, Double> monthAverage = new HashMap<>();
        valuesByMonth.forEach((month, values) -> monthAverage.put(month, values.stream().reduce(Double::sum).get() / values.size()));
        dataByYear.forEach((k, v) -> dataByYear.put(k, v.stream().sorted(Comparator.comparing(NumericNumericDTO::getValueX)).collect(Collectors.toList())));

        LinkedHashMap<String, LinkedHashMap<String, Double>> stackedResult = new LinkedHashMap<>();
        stackedValues.forEach((month, values) -> {
            String monthName = DireQueryUtils.getMonthName(month, Locale.ITALY);
            stackedResult.put(monthName, new LinkedHashMap<>());
            List<Integer> sortedYears = values.keySet().stream().mapToInt(Integer::parseInt).sorted().boxed().collect(Collectors.toList());
            sortedYears.forEach(year -> stackedResult.get(monthName).put(String.valueOf(year), values.get(String.valueOf(year))));
        });

        List<Integer> sorted = sameTimeIntervalExpenses.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList());
        _calcVariationFromPreviousYear(sameTimeIntervalExpenses, sorted);
        return new SumFieldByYearAndMonthDTO(dataByYear, monthAverage, sameTimeIntervalExpenses, stackedResult);
    }

    private void _calcVariationFromPreviousYear(Map<Integer, SameTimeIntervalCountDTO> sameTimeIntervalExpenses, List<Integer> sorted) {
        for (int i = 0; i < sorted.size(); i++) {
            if (i == 0) {
                sameTimeIntervalExpenses.get(sorted.get(0)).setVariationPercentage(100);
                continue;
            }
            int previousYear = sorted.get(i - 1);
            SameTimeIntervalCountDTO previousYearExpenses = sameTimeIntervalExpenses.get(previousYear);
            int currentYear = sorted.get(i);
            SameTimeIntervalCountDTO currentYearExpenses = sameTimeIntervalExpenses.get(currentYear);
            double variation = (currentYearExpenses.getExpenses() - previousYearExpenses.getExpenses())
                    / previousYearExpenses.getExpenses() * 100;
            currentYearExpenses.setVariationPercentage(variation);
            sameTimeIntervalExpenses.put(currentYear, currentYearExpenses);
        }
    }

    public Map<Integer, Double> sumFieldByYear(Map<Integer, List<NumericNumericDTO>> byYearAndMonth) {
        Map<Integer, Double> res = new HashMap<>();
        byYearAndMonth.forEach((k, v) ->
                res.put(k, v.stream().map(NumericNumericDTO::getValueY).reduce(0.0, Double::sum))
        );
        return res;
    }

    private List<ValueGroupByDTO> _queryFieldAndSumByYearAndMonth(List<AggregationOperation> yearsAndAddressesFilters, String fieldToBeSummed, String timeField, ThmDireEntity entity) {

        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        aggregationsPipeline.add(Aggregation.project()
                .andExpression("data." + timeField).as("data")
                .andExpression("data." + fieldToBeSummed).as("toBeSummed")
        );

        aggregationsPipeline.add(Aggregation.addFields()
                .addField("year").withValue(year(toDate("$data")))
                .addField("month").withValue(month(toDate("$data")))
                .build());
        aggregationsPipeline.add(Aggregation.group("year", "month").sum("toBeSummed").as("value"));

        return sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                entity.pitagoraName(), ValueGroupByDTO.class).getMappedResults();
    }

    public List<AggregationOperation> buildCommonYearsAndAddressesFilters(MaintenanceRequestDTO dto, List<String> dateFields) {
        return _buildCommonYearsAndAddressesFilters(dto, dateFields, "");
    }

    public List<AggregationOperation> buildCommonYearsAndAddressesFiltersOnLookedUpDataset(MaintenanceRequestDTO dto, List<String> dateFields) {
        return _buildCommonYearsAndAddressesFilters(dto, dateFields, "aggregations.");
    }

    private List<AggregationOperation> _buildCommonYearsAndAddressesFilters(MaintenanceRequestDTO dto, List<String> dateFields, String fieldPrefix) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>();

        List<Criteria> addressesAndDatesNotNullCriteria = new ArrayList<>();
        addressesAndDatesNotNullCriteria.addAll(dateFields.stream()
                .map(dateField -> Criteria.where(fieldPrefix + "data." + dateField).ne(null))
                .collect(Collectors.toList()));
        addressesAndDatesNotNullCriteria.add(Criteria.where(fieldPrefix + "data.sede").in(dto.getAddresses()));
        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(addressesAndDatesNotNullCriteria)));

        AtomicReference<AddFieldsOperationBuilder> calcYear = new AtomicReference<>(Aggregation.addFields());
        dateFields.forEach(field -> calcYear.set(calcYear.get()
                .addField("year_" + field).withValue(year(toDate("$data." + field))))
        );
        aggregationsPipeline.add(calcYear.get().build());

        aggregationsPipeline.add(Aggregation.match(new Criteria().orOperator(
                dateFields.stream().map(field -> Criteria.where("year_" + field).in(dto.getYears())).collect(Collectors.toList())
        )));

        return aggregationsPipeline;
    }

    public static String getMonthName(Integer month, Locale language) {
        return Month.of(month).getDisplayName(TextStyle.FULL_STANDALONE, language);
    }
}
