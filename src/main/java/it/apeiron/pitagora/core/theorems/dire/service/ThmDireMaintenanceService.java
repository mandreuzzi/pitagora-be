package it.apeiron.pitagora.core.theorems.dire.service;

import it.apeiron.pitagora.core.dto.charts.ChartsDTO.ChartType;
import it.apeiron.pitagora.core.dto.charts.*;
import it.apeiron.pitagora.core.entity.Filter;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.DireQueryUtils;
import it.apeiron.pitagora.core.theorems.dire.DireUserPreferences;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import it.apeiron.pitagora.core.theorems.dire.dto.*;
import it.apeiron.pitagora.core.theorems.dire.dto.query.CategoryValueDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.ExpensesWithSiteMonthYearDTO;
import it.apeiron.pitagora.core.util.QueryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static it.apeiron.pitagora.core.theorems.dire.DireQueryUtils.THOUSANDS_CONVERTER;
import static org.springframework.data.mongodb.core.aggregation.ArrayOperators.ArrayElemAt.arrayOf;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Month.month;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.year;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmDireMaintenanceService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public ScheduledMaintenanceDTO getScheduledMaintenance(MaintenanceRequestDTO dto) {
        List<AggregationOperation> yearsAndAddressesFilters = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(dto, List.of("reviewedAt", "dataIntervento"));
        List<HistogramCategoryDTO> activitiesProgress = sp.direQueryUtils.categoriesWithCountByField(
                        ThmDireEntity.SCHEDULED_MAINTENANCE,
                        "reviewStatus",
                        yearsAndAddressesFilters,
                        false).stream().
                map(res -> new HistogramCategoryDTO(res.getCategory(), res.getValue()))
                .peek(cat -> cat.setCategory(cat.getCategory().replace("Attivita", "Attività")))
                .sorted(Comparator.comparing(HistogramCategoryDTO::getCategory)).collect(Collectors.toList());

        StackedHistogramChartDTO progressBySede = getProgressByField("sede", yearsAndAddressesFilters);
        progressBySede.setStackedValues(_sortStackedCategory(progressBySede.getStackedValues()));

        HistogramChartDTO delayByCategory = getAverageDelayByField(ThmDireEntity.SCHEDULED_MAINTENANCE, "Attivita eseguita", "tag", yearsAndAddressesFilters);
        delayByCategory.calcAverage();
        delayByCategory.setTotal(delayByCategory.getAverage());

        HistogramChartDTO delayByZone = getAverageDelayByField(ThmDireEntity.SCHEDULED_MAINTENANCE,
                "Attivita eseguita", "zona", yearsAndAddressesFilters);
        delayByZone.sortByCountDesc();

        StackedHistogramChartDTO progressByCategory = getProgressByField("tag", yearsAndAddressesFilters);
        progressByCategory.setStackedValues(_sortStackedCategory(progressByCategory.getStackedValues()));
//        progressByCategory.sortOuterByStackTotalDesc();

        ScheduledMaintenanceDTO tab = ((DireUserPreferences) sp.jwtService.getUserTheoremPreferences(Theorem.DIRE)).getScheduledTab();
        tab.getActivitiesProgress().setContent(new HistogramChartDTO(activitiesProgress));
        tab.getDelayByCategory().setContent(delayByCategory);
        tab.getDelayByZone().setContent(delayByZone);
        tab.setProgressBySiteOnMap(_buildProgressBySiteOnMap(progressBySede.getStackedValues()));

        progressBySede.setStackedValues(_renameCategoryAttivita(progressBySede.getStackedValues()));
        tab.getProgressByBuilding().setContent(progressBySede);
        progressByCategory.setStackedValues(_renameCategoryAttivita(progressByCategory.getStackedValues()));
        tab.getProgressByCategory().setContent(progressByCategory);
        return tab;
    }

    public ReactiveMaintenanceDTO getReactiveMaintenance(MaintenanceRequestDTO dto) {
        List<AggregationOperation> accountingDatasetYearsAndAddressesFilters = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(dto, List.of("reviewedAt"));
        StackedHistogramChartDTO yearAndCategoryExpenses = _getExpensesBy(
                accountingDatasetYearsAndAddressesFilters,
                "reviewedAt",
                "categoria");
        yearAndCategoryExpenses.getFirstField().setDescription("Anno");
        yearAndCategoryExpenses.getSecondField().setDescription("Categoria");

        StackedHistogramChartDTO categoryAndSiteExpenses = _getExpensesBy(
                accountingDatasetYearsAndAddressesFilters,
                "sede",
                "categoria");
        categoryAndSiteExpenses.sortInnerByCount();
        categoryAndSiteExpenses.sortOuterByStackTotalDesc();
        categoryAndSiteExpenses.getFirstField().setDescription("Sede");
        categoryAndSiteExpenses.getSecondField().setDescription("Categoria");

        StackedHistogramChartDTO categoryAndComponentExpenses = _getExpensesBy(
                accountingDatasetYearsAndAddressesFilters,
                "categoria",
                "componente");
        categoryAndComponentExpenses.sortInnerByCount();
        categoryAndComponentExpenses.sortOuterByStackTotalDesc();
        categoryAndComponentExpenses.getFirstField().setDescription("Categoria");
        categoryAndComponentExpenses.getSecondField().setDescription("Componente");

        StackedHistogramChartDTO yearAndLocalizationExpenses = _getExpensesBy(
                accountingDatasetYearsAndAddressesFilters,
                "reviewedAt",
                "piano");
        yearAndLocalizationExpenses.sortInnerByCount();
        yearAndCategoryExpenses.sortByOuterCategory();
        yearAndCategoryExpenses.getFirstField().setDescription("Anno");
        yearAndCategoryExpenses.getSecondField().setDescription("Localizzazione");

        List<AggregationOperation> maintenanceYearsAndAddressesFilters = sp.direQueryUtils.buildCommonYearsAndAddressesFilters(dto, List.of("reviewedAt", "dataIntervento"));
        HistogramChartDTO delayByComponent = getAverageDelayByField(ThmDireEntity.REACTIVE_MAINTENANCE, "Chiusura del Ticket", "componente",
                maintenanceYearsAndAddressesFilters);
        delayByComponent.calcAverage();

        HistogramChartDTO tickets = _getAllTicketsByYear(maintenanceYearsAndAddressesFilters);
        tickets.sortByCategoryAsc();

        ReactiveMaintenanceHelper helper = _getMonthExpenses(accountingDatasetYearsAndAddressesFilters, dto);
        LinearChartDTO monthExpenses = helper.monthExpenses;

        ReactiveMaintenanceDTO tab = ((DireUserPreferences) sp.jwtService.getUserTheoremPreferences(Theorem.DIRE)).getReactiveTab();
        tab.getMonthExpenses().setContent(monthExpenses);
        tab.getMonthExpensesInfo().setContent(helper.expensesByMonthInfo);
        tab.getTickets().setContent(tickets);
        tab.getExpensesByYearAndCategory().setContent(yearAndCategoryExpenses);
        tab.getExpensesBySiteAndCategory().setContent(categoryAndSiteExpenses);
        tab.getExpensesByCategoryAndComponent().setContent(categoryAndComponentExpenses);
        tab.getExpensesByYearAndLocalization().setContent(yearAndLocalizationExpenses);
        tab.getTicketTimeVsExpenses().setContent(_getTicketTimeVsField(accountingDatasetYearsAndAddressesFilters, sp.direQueryUtils.buildCommonYearsAndAddressesFiltersOnLookedUpDataset(dto, List.of("reviewedAt", "dataIntervento"))));
        tab.getDelayByComponent().setContent(delayByComponent);
        tab.setExpensesBySiteOnMap(_buildExpensesBySiteOnMap(categoryAndSiteExpenses.getStackedValues()));
        return tab;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Double>> _sortStackedCategory(LinkedHashMap<String, LinkedHashMap<String, Double>> categoryProgress) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> newStackedValues = new LinkedHashMap<>();
//        categoryProgress.forEach((key, value) ->
//        {
//            if (!value.containsKey("Attivita eseguita")) {
//                categoryProgress.get(key).put("Attivita eseguita", 0D);
//            }
//        });

        categoryProgress.forEach((site, value) -> {
            LinkedHashMap<String, Double> values = categoryProgress.get(site);
            LinkedHashMap<String, Double> sortedResults = new LinkedHashMap<>();

            values.keySet().stream().sorted().forEach(category -> {
//                Double result = values.get(category);
//                if (category.equals("Attivita programmata")) {
//                    result = result - values.get("Attivita eseguita");
//                }
                sortedResults.put(category, values.get(category));
            });
            newStackedValues.put(site, sortedResults);
        });
        return newStackedValues;
    }

    private List<SiteDTO> _buildProgressBySiteOnMap(LinkedHashMap<String, LinkedHashMap<String, Double>> progressBySite) {
        List<String> sedeToMatch = new ArrayList<>();
        if (progressBySite != null) {
            sedeToMatch = new ArrayList<>(progressBySite.keySet());
        }
        List<SiteDTO> results = sp.direQueryUtils.getAllSites(List.of(ThmDireEntity.SCHEDULED_MAINTENANCE), sedeToMatch).stream()
                .map(record -> new SiteDTO(record.getData())).collect(Collectors.toList());

        if (progressBySite == null) {
            return results;
        }
        results.forEach(sede -> {
            Map<String, Double> categCount = progressBySite.get(sede.getSede());
            double total = 0;
            double completed = 0;
            double pendingActivitiesPercentage = 0;
            if (categCount != null) {
                total = categCount.values().stream().reduce(0.0, Double::sum);
                completed = categCount.get("Attivita eseguita") != null ? categCount.get("Attivita eseguita") : 0;
                pendingActivitiesPercentage = 1 - completed / total;
            }
            sede.setMapMarkerRadius(pendingActivitiesPercentage);
            sede.setMetadata(new MaintenanceActivityProgressDTO(total, completed));
        });
        return results;
    }

    private List<SiteDTO> _buildExpensesBySiteOnMap(LinkedHashMap<String, LinkedHashMap<String, Double>> expensesBySite) {
        List<SiteDTO> results = sp.direQueryUtils.getAllSites(List.of(ThmDireEntity.REACTIVE_MAINTENANCE), new ArrayList<>(expensesBySite.keySet())).stream()
                .map(record -> new SiteDTO(record.getData())).collect(Collectors.toList());
        results.forEach(sede -> {
            Map<String, Double> expensesCount = expensesBySite.get(sede.getSede());
            double total = 0;
            if (expensesCount != null) {
                total = expensesCount.values().stream().reduce(0.0, Double::sum);
            }
            sede.setMetadata(total);
        });

        return SiteDTO.calcMarkerRadiusViaMetadata(results);
    }


    private ReactiveMaintenanceHelper _getMonthExpenses(List<AggregationOperation> yearsAndAddressesFilters, MaintenanceRequestDTO dto) {

        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        aggregationsPipeline.add(Aggregation.project()
                .andExpression("data.reviewedAt").as("reviewedAt")
                .andExpression("data.sede").as("sede")
                .andExpression("data.costi").divide(THOUSANDS_CONVERTER).as("costi")
        );
        aggregationsPipeline.add(Aggregation.addFields()
                .addField("month").withValue(month(toDate("$reviewedAt")))
                .addField("year").withValue(year(toDate("$reviewedAt")))
                .build());

        List<ExpensesWithSiteMonthYearDTO> res = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.REACTIVE_ACCOUNTING.pitagoraName(), ExpensesWithSiteMonthYearDTO.class)
                .getMappedResults();

        Map<Integer, Map<Integer, Double>> expenses = new HashMap<>();
        res.forEach(exp -> {
            if (!expenses.containsKey(exp.getYear())) {
                expenses.put(exp.getYear(), new HashMap<>(Map.of(exp.getMonth(), exp.getCosti())));
            } else if (!expenses.get(exp.getYear()).containsKey(exp.getMonth())) {
                expenses.get(exp.getYear()).put(exp.getMonth(), exp.getCosti());
            } else {
                double curr = expenses.get(exp.getYear()).get(exp.getMonth());
                expenses.get(exp.getYear()).put(exp.getMonth(), curr + exp.getCosti());
            }
        });

        Double totalExpenses = res.stream().mapToDouble(ExpensesWithSiteMonthYearDTO::getCosti).sum();

        LinearChartDTO chart = new LinearChartDTO();
        chart.setValueFieldX(ModelField.builder().description("Data").type(FieldType.TIMESTAMP).build());
        chart.setValueFieldY(ModelField.builder().description("Costi").type(FieldType.DOUBLE).build());
        chart.setNumericNumeric(new ArrayList<>());
        chart.setTotal(totalExpenses);
        expenses.forEach((year, months) ->
                months.forEach((month, exp) -> {
                    chart.getNumericNumeric().add(
                            NumericNumericDTO.builder()
                                    .valueX(LocalDateTime.of(year, month, 1, 0, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli())
                                    .valueY(exp)
                                    .build());
                }));

        chart.addLinearRegression();
        chart.addMeanY();
        _setTotalOnCurrentYearAndCurrentYearExpensesDecreaseOnSamePeriodOfPreviousYear(dto, chart);

        ReactiveMaintenanceHelper helper = new ReactiveMaintenanceHelper(chart);
        this._getMinYearAnd(expenses, helper);

        return helper;
    }

    private void _getMinYearAnd(Map<Integer, Map<Integer, Double>> expenses, ReactiveMaintenanceHelper helper) {
        Integer minYear = expenses.keySet().stream().min(Integer::compareTo).orElse(null);
        if (minYear != null) {
            Integer maxYear = expenses.keySet().stream().max(Integer::compareTo).get();

            Integer firstMonth = expenses.get(minYear).keySet().stream().min(Integer::compareTo).get();
            Integer lastMonth = expenses.get(maxYear).keySet().stream().max(Integer::compareTo).get();
            Double variation = _calcVariation(expenses.get(maxYear).get(lastMonth), expenses.get(minYear).get(firstMonth));

            helper.expensesByMonthInfo = new ReactiveMaintenanceExpensesByMonthInfoDTO(variation, minYear, maxYear, DireQueryUtils.getMonthName(firstMonth, Locale.ITALIAN), DireQueryUtils.getMonthName(lastMonth, Locale.ITALIAN));
        }
    }

    private void _setTotalOnCurrentYearAndCurrentYearExpensesDecreaseOnSamePeriodOfPreviousYear(MaintenanceRequestDTO dto, LinearChartDTO chart) {

        Integer currentYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(new Date()));
        Integer previousYear = currentYear - 1;
        Map<Integer, Double> currentAndPreviusYearTotalExpenses = new HashMap<>(Map.of(currentYear, 0D, previousYear, 0D));
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        MaintenanceRequestDTO newDTO = new MaintenanceRequestDTO(List.of(currentYear, previousYear), dto.getAddresses());
        List<AggregationOperation> addressesYearsFilter = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(newDTO, List.of("reviewedAt"));

        LinkedHashMap<String, LinkedHashMap<String, Double>> expensesByMonthAndYearOnPreviousAndCurrentYear = new LinkedHashMap<>(sp.direQueryUtils.sumFieldByYearAndMonth(addressesYearsFilter, "costi", "reviewedAt", ThmDireEntity.REACTIVE_ACCOUNTING).getStackedValues());

        int currentMonth = cal.get(Calendar.MONTH) + 1; //Month start from number 0
        for (int i = 1; i <= currentMonth; i++) {
            String currentMonthName = DireQueryUtils.getMonthName(i, Locale.ITALY);;
            LinkedHashMap<String, Double> reactiveAccountingMonth = expensesByMonthAndYearOnPreviousAndCurrentYear.get(currentMonthName);

            currentAndPreviusYearTotalExpenses.keySet().forEach(year -> {
                String stringYear = String.valueOf(year);
                Double reactiveAccountingValueCurrentMonthAndYear = Optional.ofNullable(reactiveAccountingMonth.get(stringYear)).orElse(0D);

                currentAndPreviusYearTotalExpenses.put(year, currentAndPreviusYearTotalExpenses.get(year) + reactiveAccountingValueCurrentMonthAndYear);
            });
        }

        chart.setCurrentYearVariationComparedToPreviousYear(_calcVariation(currentAndPreviusYearTotalExpenses.get(currentYear), currentAndPreviusYearTotalExpenses.get(previousYear)));
        chart.setTotal(currentAndPreviusYearTotalExpenses.get(currentYear) / THOUSANDS_CONVERTER);
    }

    private static Double _calcVariation(Double currentYearExpenses, Double previousYearExpenses) {
        return (currentYearExpenses - previousYearExpenses)
                / previousYearExpenses * 100;
    }

    private HistogramChartDTO _getAllTicketsByYear(List<AggregationOperation> yearsAndAddressesFilters) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        aggregationsPipeline.add(Aggregation.project()
                .andExpression("_id").as("category")
                .andExpression("data.reviewedAt").as("reviewedAt")
        );
        aggregationsPipeline.add(Aggregation.addFields()
                .addField("year").withValue(year(toDate("$reviewedAt")))
                .build());
        aggregationsPipeline.add(Aggregation.group("year").count().as("value"));

        List<HistogramCategoryDTO> results = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.REACTIVE_MAINTENANCE.pitagoraName(), CategoryValueDTO.class)
                .getMappedResults().stream().
                map(res ->
                        new HistogramCategoryDTO(res.getCategory(), res.getValue())
                ).collect(Collectors.toList());

        HistogramChartDTO chart = new HistogramChartDTO(results);
        AtomicReference<Double> sum = new AtomicReference<>(0D);
        results.forEach(result -> sum.set(result.getCount() + sum.get()));
        chart.setTotal(sum.get());
        return chart;
    }

    private StackedHistogramChartDTO getProgressByField(String field, List<AggregationOperation> yearsAndAddressesFilters) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        List<PitagoraData> mappedResults = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName(), PitagoraData.class)
                .getMappedResults();

        PitagoraModel scheduledMaintModel = sp.modelService.findByName(ThmDireEntity.SCHEDULED_MAINTENANCE.pitagoraName()).get();
        FieldDataDTO fieldX = FieldDataDTO.builder().field(scheduledMaintModel.getStructure().get(field)).values(mappedResults.stream().map(r -> r.getData().get(field)).collect(
                Collectors.toList())).build();
        FieldDataDTO fieldY = FieldDataDTO.builder().field(scheduledMaintModel.getStructure().get("reviewStatus")).values(mappedResults.stream().map(r -> r.getData().get("reviewStatus")).collect(
                Collectors.toList())).build();
        StackedHistogramChartDTO stackedChart = sp.chartsService.getStackedChart(fieldX, fieldY);

        List<Entry<String, LinkedHashMap<String, Double>>> sorted = new ArrayList<>(
                stackedChart.getStackedValues().entrySet());

        sorted.sort(Comparator.comparingDouble(
                (Entry<String, LinkedHashMap<String, Double>> e) ->
                        e.getValue().values().stream().reduce(0.0, Double::sum)));

        LinkedHashMap<String, LinkedHashMap<String, Double>> sortedCategories = new LinkedHashMap<>();
        sorted.forEach(entry -> sortedCategories.put(entry.getKey(), entry.getValue()));
        stackedChart.setStackedValues(sortedCategories);
        return stackedChart;
    }

    public HistogramChartDTO getAverageDelayByField(ThmDireEntity dataset, String reviewStatusValue, String field, List<AggregationOperation> yearsAndAddressesFilters) {

        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        Filter reviewStatusFilter = Filter.builder().field("reviewStatus").fieldType(FieldType.STRING)
                .operation(Operation.EQUALS).value(reviewStatusValue).build();
        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(QueryUtils.buildQueryCriteria(reviewStatusFilter, "data." + reviewStatusFilter.getField()))));


        aggregationsPipeline.add(Aggregation.project("data." + field)
                .and("data.reviewedAt").minus("data.dataIntervento").as("delayInMilliSec"));
        aggregationsPipeline.add(Aggregation.project(field)
                .and("delayInMilliSec").divide(86400000).as("delayInDays"));
        aggregationsPipeline.add(Aggregation.group(field).avg("delayInDays").as("avgDelayInDays"));
        aggregationsPipeline.add(Aggregation.project()
                .andExpression("_id").as("category")
                .andExpression("avgDelayInDays").as("value"));
        aggregationsPipeline.add(Aggregation.sort(Sort.by(Direction.DESC, "value")));
        aggregationsPipeline.add(Aggregation.limit(50));


        List<HistogramCategoryDTO> results = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        dataset.pitagoraName(), CategoryValueDTO.class)
                .getMappedResults().stream().
                map(res ->
                        new HistogramCategoryDTO(res.getCategory(), res.getValue())
                ).collect(Collectors.toList());

        return new HistogramChartDTO(results);
    }

    private LinkedHashMap<String, LinkedHashMap<String, Double>> _getMapOfExpensesBy(List<AggregationOperation> yearsAndAddressesFilters, String firstField, String secondField) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        ProjectionOperation projection = Aggregation.project()
                .andExpression("data." + firstField).as(firstField)
                .andExpression("data.costi").as("costi");

        if (secondField != null) {
            projection = projection.andExpression("data." + secondField).as(secondField);
        }

        aggregationsPipeline.add(projection);

        AtomicReference<String> firstFieldToGroup = new AtomicReference<>(firstField);
        if (firstField.equals("reviewedAt")) {

            aggregationsPipeline.add(Aggregation.addFields()
                    .addField("year").withValue(year(toDate("$reviewedAt")))
                    .build());
            firstFieldToGroup.set("year");
        }

        if (secondField != null) {
            aggregationsPipeline.add(Aggregation.group(firstFieldToGroup.get(), secondField).sum("costi").as("costi"));
        } else {
            aggregationsPipeline.add(Aggregation.group(firstFieldToGroup.get()).sum("costi").as("costi"));
        }


        List<LinkedHashMap> result = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.REACTIVE_ACCOUNTING.pitagoraName(), LinkedHashMap.class)
                .getMappedResults();

        LinkedHashMap<String, LinkedHashMap<String, Double>> results = new LinkedHashMap<>();

        result.forEach(mappa -> {
            LinkedHashMap categories = (LinkedHashMap) mappa.get("_id");
            String outerStack = _getStackValue(categories.get(firstFieldToGroup.get()));
            String innerStack = _getStackValue(categories.get(secondField));

            if (!results.containsKey(outerStack)) {
                results.put(outerStack, new LinkedHashMap<>());
            }
            results.get(outerStack).put(innerStack, (Double) mappa.get("costi"));

        });
        return results;
    }

    private StackedHistogramChartDTO _getExpensesBy(List<AggregationOperation> yearsAndAddressesFilters, String firstField, String secondField) {

        LinkedHashMap<String, LinkedHashMap<String, Double>> results = _getMapOfExpensesBy(yearsAndAddressesFilters, firstField, secondField);

        results.keySet().forEach(key -> results.get(key).forEach((internalKey, expenses) -> results.get(key).put(internalKey, expenses / THOUSANDS_CONVERTER)));
        AtomicReference<Double> noneCategorytotalValue = new AtomicReference<>(0D);
        results.forEach((key, values) -> {
            if (values.get("(Vuoto)") != null) {
                noneCategorytotalValue.set(noneCategorytotalValue.get() + values.get("(Vuoto)"));
            }
        });

        return StackedHistogramChartDTO.builder()
                .chartType(ChartType.STACKED_HISTOGRAM)
                .stackedValues(sp.thmDireService.sortInnerByValueAndOuterByInner(results))
                .firstField(ModelField.builder().type(FieldType.STRING).build())
                .secondField(ModelField.builder().type(FieldType.STRING).build())
                .total(noneCategorytotalValue.get())
                .build();
    }

    private String _getStackValue(Object value) {
        return value == null || value.equals("null") ? "(Vuoto)" : value.toString();
    }

    private LinearChartDTO _getTicketTimeVsField(List<AggregationOperation> yearsAndAddressesFilters,
                                                 List<AggregationOperation> aggregationYearsAndAddressesFilters) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(yearsAndAddressesFilters);

        aggregationsPipeline.add(Aggregation.lookup(ThmDireEntity.REACTIVE_MAINTENANCE.pitagoraName(), "data.ticketId", "data.ticketId", "aggregations"));

        Filter reviewStatusFilter = Filter.builder().field("reviewStatus").fieldType(FieldType.STRING).operation(Operation.EQUALS)
                .value("Chiusura del Ticket").build();
        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(
                QueryUtils.buildQueryCriteria(reviewStatusFilter, "data." + reviewStatusFilter.getField()))));

        aggregationsPipeline.addAll(aggregationYearsAndAddressesFilters);

        aggregationsPipeline.add(Aggregation.project()
                .andExpression("data.costi").as("costi")
                .and(arrayOf("aggregations.data.reviewedAt").elementAt(0)).as("reviewedAt")
                .and(arrayOf("aggregations.data.dataIntervento").elementAt(0)).as("dataIntervento")
        );
        aggregationsPipeline.add(Aggregation.project("costi").and("reviewedAt").minus("dataIntervento").as("timeInMilliSec"));
        aggregationsPipeline.add(Aggregation.project("costi").and("timeInMilliSec").divide(86400000).as("timeInDays"));


        List<Map> results = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsPipeline),
                        ThmDireEntity.REACTIVE_ACCOUNTING.pitagoraName(), Map.class)
                .getMappedResults();

        LinearChartDTO chart = LinearChartDTO.builder()
                .chartType(ChartType.LINEAR)
                .valueFieldX(ModelField.builder().type(FieldType.DOUBLE).description("Tempo chiusura Ticket (gg)").build())
                .valueFieldY(ModelField.builder().type(FieldType.DOUBLE).description("Costi (€)").build())
                .numericNumeric(results.stream()
                        .map(r -> new NumericNumericDTO((double) r.get("timeInDays"), (double) r.get("costi"))).collect(
                                Collectors.toList()))
                .build();

        chart.getNumericNumeric().sort(Comparator.comparing(NumericNumericDTO::getValueX));

        chart.setTotal(chart.addMeanX());
        chart.addMeanY();

        return chart;
    }

    private LinkedHashMap<String, LinkedHashMap<String, Double>> _renameCategoryAttivita (LinkedHashMap<String, LinkedHashMap<String, Double>> oldMap) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> newMap = new LinkedHashMap<>();
        oldMap.forEach((outerKey, outerValue) -> {
            LinkedHashMap<String, Double> internalMap = new LinkedHashMap<>();
            outerValue.forEach((innerKey, innerValue) -> {
                internalMap.put(innerKey.replace("Attivita", "Attività"), innerValue);
            });
            newMap.put(outerKey, internalMap);
        });
        return newMap;
    }


    private static class ReactiveMaintenanceHelper {
        private LinearChartDTO monthExpenses;

        ReactiveMaintenanceExpensesByMonthInfoDTO expensesByMonthInfo;

        private ReactiveMaintenanceHelper(LinearChartDTO monthExpenses) {
            this.monthExpenses = monthExpenses;
            this.expensesByMonthInfo = new ReactiveMaintenanceExpensesByMonthInfoDTO();
        }
    }

}
