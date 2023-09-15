package it.apeiron.pitagora.core.theorems.dire.service;

import it.apeiron.pitagora.core.dto.charts.HistogramCategoryDTO;
import it.apeiron.pitagora.core.dto.charts.HistogramChartDTO;
import it.apeiron.pitagora.core.dto.charts.StackedHistogramChartDTO;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.DireQueryUtils;
import it.apeiron.pitagora.core.theorems.dire.DireUserPreferences;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import it.apeiron.pitagora.core.theorems.dire.dto.*;
import it.apeiron.pitagora.core.theorems.dire.dto.query.ValueGroupByDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.ValueGroupBySiteMonthAndYearDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static it.apeiron.pitagora.core.theorems.dire.DireQueryUtils.THOUSANDS_CONVERTER;
import static it.apeiron.pitagora.core.theorems.dire.ThmDireEntity.*;
import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Month.month;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.year;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmDireFinanceService {

    private ServiceProvider sp;
    private static final String TOTAL_EXPENSES_FIELD = "Spesa Totale";
    private static final String CURRENT_YEAR_CATEGORY = new SimpleDateFormat("yyyy").format(new Date());

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public FinanceTabDTO getFinanceTab(MaintenanceRequestDTO dto) {

        StackedHistogramChartDTO expenseItemAnalysis = _getExpensesItemAnalysis(dto);
        HistogramChartDTO totalExpensesByYears = _getTotalExpensesByYears(expenseItemAnalysis.getStackedValues());
        HistogramChartDTO expensesStats = _getExpensesStats(expenseItemAnalysis.getStackedValues());
        StackedHistogramChartDTO totalExpensesByMonth = _getTotalExpensesByMonth(dto);
        StackedHistogramChartDTO avgExpensesBySite = _getAvgExpensesBySite(dto);
        List<SedeExpensesInfoDTO> expensesBySiteStats = _getExpensesBySedeTable(dto);
        FinanceHelper financeHelper = getFinanceExpensesSiteLabel(dto);
        FinanceTableSiteExpensesLabelDTO siteExpensesLabel = financeHelper.financeTableSiteExpensesLabelDTO;
        List<SiteDTO> totalExpensesBySiteOnMap = _getTotalExpensesBySiteOnMap(financeHelper.siteAndTotalExpenses);

        FinanceTabDTO tab = ((DireUserPreferences) sp.jwtService.getUserTheoremPreferences(Theorem.DIRE)).getFinanceTab();
        tab.getTotalExpensesByYears().setContent(totalExpensesByYears);
        tab.getTotalExpensesByMonth().setContent(totalExpensesByMonth);
        tab.getExpenseItemAnalysis().setContent(expenseItemAnalysis);
        tab.getExpensesStats().setContent(expensesStats);
        tab.getAvgExpensesBySite().setContent(avgExpensesBySite);
        tab.getExpensesBySiteStats().setContent(expensesBySiteStats);
        tab.getSiteExpensesStatisticsLabel().setContent(siteExpensesLabel);
        tab.setTotalExpensesBySiteOnMap(SiteDTO.calcMarkerRadiusViaMetadata(totalExpensesBySiteOnMap));
        return tab;
    }

    private List<SiteDTO> _getTotalExpensesBySiteOnMap(Map<String, Double> siteAndTotalExpenses) {

        List<SiteDTO> allSites = sp.mongoTemplateData.findAll(PitagoraData.class, SITE.pitagoraName()).stream().filter(site -> siteAndTotalExpenses.containsKey(site.getData().get("sede")))
                .map(site -> new SiteDTO((String) site.getData().get("sede"), (String) site.getData().get("description"), (Double) site.getData().get("latitude"),
                        (Double) site.getData().get("longitude"), siteAndTotalExpenses.get(site.getData().get("sede"))))
                .collect(Collectors.toList());

        return allSites;
    }

    private FinanceHelper getFinanceExpensesSiteLabel(MaintenanceRequestDTO dto) {
        Map<String, Double> siteWithExpensesAnnualIncrease = new HashMap<>();
        Map<String, Double> siteWithExpensesAnnualDecrease = new HashMap<>();
        Map<String, Double> siteWithAverageExpenditurePerSquareMeter = new HashMap<>();
        Map<String, Double> siteAndTotalExpenses = new HashMap<>();
        AtomicReference<Double> totalExpensesOfSites = new AtomicReference<>(0D);

        Map<String, Double> allSitesSurface = new HashMap<>();
        sp.mongoTemplateData.findAll(PitagoraData.class, SITE.pitagoraName()).stream()
                .filter(data -> data.getData().get("surface") != null)
                .forEach(data -> allSitesSurface.put((String) data.getData().get("sede"), (double) data.getData().get("surface")));

        dto.getAddresses().forEach(address -> {
            MaintenanceRequestDTO dtoWithOnlyCurrentAddress = new MaintenanceRequestDTO(dto.getYears(), List.of(address));

            List<HistogramCategoryDTO> dataOnCurrentSite = _getTotalExpensesByYears(_getExpensesItemAnalysis(dtoWithOnlyCurrentAddress).getStackedValues()).getCategories();
            double maxVariation = dataOnCurrentSite.stream().mapToDouble(HistogramCategoryDTO::getCustomValue).max().orElse(0D);
            double minVariation = dataOnCurrentSite.stream().mapToDouble(HistogramCategoryDTO::getCustomValue).min().orElse(0D);

            siteWithExpensesAnnualIncrease.put(address, maxVariation);
            siteWithExpensesAnnualDecrease.put(address, minVariation);

            Double totalExpenses = dataOnCurrentSite.stream().mapToDouble(HistogramCategoryDTO::getCount).sum();
            if (allSitesSurface.containsKey(address)) {
                Double averageExpensesBySurfaceOfSite = totalExpenses / allSitesSurface.get(address);
                siteWithAverageExpenditurePerSquareMeter.put(address, averageExpensesBySurfaceOfSite);
            }

            //get total expenses of this site
            siteAndTotalExpenses.put(address, totalExpenses);

            totalExpensesOfSites.set(totalExpensesOfSites.get() + totalExpenses);
        });
        Double maxVariationByAllSite = siteWithExpensesAnnualIncrease.values().stream().max(Comparator.comparing(Double::doubleValue)).orElse(0D);
        Double minVariationByAllSite = siteWithExpensesAnnualDecrease.values().stream().min(Comparator.comparing(Double::doubleValue)).orElse(0D);

        String siteWithTheLargestIncreaseAnnualExpenses = siteWithExpensesAnnualIncrease.entrySet().stream().filter(entry -> maxVariationByAllSite.equals(entry.getValue())).map(Map.Entry::getKey).findFirst().get();
        String siteWithHighestAverageExpenditurePerSquareMeter = siteWithAverageExpenditurePerSquareMeter.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();

        int minYear = dto.getYears().stream().mapToInt(Integer::valueOf).min().getAsInt();
        int maxYear = dto.getYears().stream().mapToInt(Integer::valueOf).max().getAsInt();

        FinanceTableSiteExpensesLabelDTO financeTableSiteExpensesLabel = new FinanceTableSiteExpensesLabelDTO(siteWithTheLargestIncreaseAnnualExpenses, siteWithHighestAverageExpenditurePerSquareMeter, minYear, maxYear);

        SiteAndValueDTO siteWithMaxOrMinExpensesIncreaseOrDecrease = new SiteAndValueDTO(siteWithExpensesAnnualDecrease.entrySet().stream().filter(entry -> minVariationByAllSite.equals(entry.getValue())).findFirst().get());

        financeTableSiteExpensesLabel.setSiteWithMaxExpensesIncreaseOrDecrease(new SiteAndValueDTO(siteWithTheLargestIncreaseAnnualExpenses, siteWithExpensesAnnualIncrease.get(siteWithTheLargestIncreaseAnnualExpenses)));
        financeTableSiteExpensesLabel.setSiteWithMaxOrMinExpensesIncreaseOrDecrease(siteWithMaxOrMinExpensesIncreaseOrDecrease);

        if (maxVariationByAllSite > 0 && minVariationByAllSite > 0) {
            financeTableSiteExpensesLabel.setIncreaseOrDecrease(FinanceTableSiteExpensesLabelDTO.ONLY_INCREASE);
        } else if (maxVariationByAllSite > 0 && minVariationByAllSite < 0) {
            financeTableSiteExpensesLabel.setIncreaseOrDecrease(FinanceTableSiteExpensesLabelDTO.INCREASE_AND_DECREASE);
        } else {
            financeTableSiteExpensesLabel.setIncreaseOrDecrease(FinanceTableSiteExpensesLabelDTO.ONLY_DECREASE);
        }

        Map.Entry<String, Double> siteWithMaxExpenses = siteAndTotalExpenses.entrySet().stream().max(Map.Entry.comparingByValue()).stream().findFirst().get();
        Double value = (siteWithMaxExpenses.getValue() * 100) / totalExpensesOfSites.get();
        financeTableSiteExpensesLabel.setSiteWithMaxExpenses(new SiteAndValueDTO(siteWithMaxExpenses.getKey(), value));

        FinanceHelper financeHelper = new FinanceHelper();
        financeHelper.siteAndTotalExpenses = siteAndTotalExpenses;
        financeHelper.financeTableSiteExpensesLabelDTO = financeTableSiteExpensesLabel;
        return financeHelper;
    }

    private List<SedeExpensesInfoDTO> _getExpensesBySedeTable(MaintenanceRequestDTO dto) {
        List<SedeExpensesInfoDTO> expensesBySedeInfo = new ArrayList<>();

        List<ValueGroupBySiteMonthAndYearDTO> electricityExpenses = buildQueryForSiteMonthAndYear(dto, "periodo", "costoII", ENERGY_ELECTRICITY_EXPENSES, true);
        List<ValueGroupBySiteMonthAndYearDTO> gasExpenses = buildQueryForSiteMonthAndYear(dto, "data", "ricaviVendita", ENERGY_GAS_CONSUMPTION, true);
        List<ValueGroupBySiteMonthAndYearDTO> maintenanceExpenses = buildQueryForSiteMonthAndYear(dto, "reviewedAt", "costi", REACTIVE_ACCOUNTING, true);

        LinkedHashMap<String, LinkedHashMap<String, Double>> expensesInfo = new LinkedHashMap<>();
        extractDataFromList(expensesInfo, "Spesa Elettricità", electricityExpenses, true);
        extractDataFromList(expensesInfo, "Spesa Gas", gasExpenses, true);
        extractDataFromList(expensesInfo, "Spesa Manutenzione", maintenanceExpenses, true);

        expensesInfo.forEach((site, expenses) -> {
            double eleExp = Optional.ofNullable(expenses.get("Spesa Elettricità")).orElse(0D);
            double gasExp = Optional.ofNullable(expenses.get("Spesa Gas")).orElse(0D);
            double maintenanceExp = Optional.ofNullable(expenses.get("Spesa Manutenzione")).orElse(0D);
            double totalExpenses = maintenanceExp + gasExp + eleExp;
            expensesBySedeInfo.add(new SedeExpensesInfoDTO(site, eleExp, gasExp, maintenanceExp, totalExpenses));
        });

        expensesBySedeInfo.sort(Comparator.comparing(SedeExpensesInfoDTO::getTotalExpenses).reversed());

        SedeExpensesInfoDTO totalExpensesInfo = new SedeExpensesInfoDTO("Total", 0D, 0D, 0D, 0D);
        expensesBySedeInfo.forEach(expensesBySede -> {
            totalExpensesInfo.setTotalExpenses(expensesBySede.getTotalExpenses() + totalExpensesInfo.getTotalExpenses());
            totalExpensesInfo.setElectricityExpenses(expensesBySede.getElectricityExpenses() + totalExpensesInfo.getElectricityExpenses());
            totalExpensesInfo.setGasExpenses(expensesBySede.getGasExpenses() + totalExpensesInfo.getGasExpenses());
            totalExpensesInfo.setMaintenanceExpenses(expensesBySede.getMaintenanceExpenses() + totalExpensesInfo.getMaintenanceExpenses());
        });
        expensesBySedeInfo.add(totalExpensesInfo);

        return expensesBySedeInfo;
    }


    private StackedHistogramChartDTO _getAvgExpensesBySite(MaintenanceRequestDTO dto) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> allSites = new LinkedHashMap<>();

        List<ValueGroupBySiteMonthAndYearDTO> electricityExpenses = buildQueryForSiteMonthAndYear(dto, "periodo", "costoII", ENERGY_ELECTRICITY_EXPENSES, false);
        List<ValueGroupBySiteMonthAndYearDTO> gasExpenses = buildQueryForSiteMonthAndYear(dto, "data", "ricaviVendita", ENERGY_GAS_CONSUMPTION, false);
        List<ValueGroupBySiteMonthAndYearDTO> maintenanceExpenses = buildQueryForSiteMonthAndYear(dto, "reviewedAt", "costi", REACTIVE_ACCOUNTING, false);

        extractDataFromList(allSites, "Elettricità", electricityExpenses.stream().sorted(Comparator.comparing(ValueGroupBySiteMonthAndYearDTO::getMonth)).collect(Collectors.toList()), false);
        extractDataFromList(allSites, "Gas", gasExpenses.stream().sorted(Comparator.comparing(ValueGroupBySiteMonthAndYearDTO::getMonth)).collect(Collectors.toList()), false);
        extractDataFromList(allSites, "Manutenzione", maintenanceExpenses.stream().sorted(Comparator.comparing(ValueGroupBySiteMonthAndYearDTO::getMonth)).collect(Collectors.toList()), false);

        allSites.forEach((site, expenses) -> expenses.putAll(Map.of(
                "max", expenses.values().stream().mapToDouble(value -> value).max().getAsDouble(),
                "avg", expenses.values().stream().mapToDouble(value -> value).average().getAsDouble(),
                "min", expenses.values().stream().mapToDouble(value -> value).min().getAsDouble())));

        StackedHistogramChartDTO avgExpensesBySite = new StackedHistogramChartDTO();
        avgExpensesBySite.setFirstField(ModelField.builder().type(FieldType.STRING).build());
        avgExpensesBySite.setSecondField(ModelField.builder().type(FieldType.STRING).build());
        avgExpensesBySite.setStackedValues(allSites);

        return avgExpensesBySite;
    }


    private void extractDataFromList(LinkedHashMap<String, LinkedHashMap<String, Double>> mapToBeFilled, String fieldPrefix, List<ValueGroupBySiteMonthAndYearDTO> dataList, boolean extractDataOnlyBySite) {
        LinkedHashMap<String, LinkedHashMap<String, List<Double>>> entries = new LinkedHashMap<>();

        if (!extractDataOnlyBySite) {

            dataList.forEach(data -> {
                if (!entries.containsKey(data.getSite())) {
                    entries.put(data.getSite(), new LinkedHashMap<>());
                }

                String monthField = DireQueryUtils.getMonthName(Integer.parseInt(data.getMonth()), Locale.ITALY) + " - " + fieldPrefix;

                if (!entries.get(data.getSite()).containsKey(monthField)) {
                    entries.get(data.getSite()).put(monthField, new ArrayList<>());
                }
                entries.get(data.getSite()).get(monthField).add(data.getValue() / THOUSANDS_CONVERTER);
            });

            entries.forEach((site, values) -> {
                if (!mapToBeFilled.containsKey(site)) {
                    mapToBeFilled.put(site, new LinkedHashMap<>());
                }
                values.forEach((month, monthValues) -> {
                    if (!mapToBeFilled.get(site).containsKey(month)) {
                        mapToBeFilled.get(site).put(month, 0D);
                    }
                    mapToBeFilled.get(site).put(month, monthValues.stream().mapToDouble(value -> value).average().orElse(0D));
                });
            });

        } else {

            dataList.forEach(data -> {
                if (!mapToBeFilled.containsKey(data.getSite())) {
                    mapToBeFilled.put(data.getSite(), new LinkedHashMap<>());
                }
                if (!mapToBeFilled.get(data.getSite()).containsKey(fieldPrefix)) {
                    mapToBeFilled.get(data.getSite()).put(fieldPrefix, 0D);
                }
                mapToBeFilled.get(data.getSite()).put(fieldPrefix, mapToBeFilled.get(data.getSite()).get(fieldPrefix) + data.getValue());
            });
        }
    }

    private List<ValueGroupBySiteMonthAndYearDTO> buildQueryForSiteMonthAndYear(MaintenanceRequestDTO dto, String timeField, String fieldToBeSummed, ThmDireEntity entity, boolean onlyGroupBySite) {

        List<AggregationOperation> addressesYearsFilter = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(dto, List.of(timeField));
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>(addressesYearsFilter);

        aggregationsPipeline.add(Aggregation.project()
                .andExpression("data." + timeField).as("data")
                .andExpression("data." + fieldToBeSummed).as("toBeSummed")
                .andExpression("data.sede").as("site")
        );

        List<ValueGroupBySiteMonthAndYearDTO> result;

        if (!onlyGroupBySite) {
            aggregationsPipeline.add(Aggregation.addFields()
                    .addField("year").withValue(year(toDate("$data")))
                    .addField("month").withValue(month(toDate("$data")))
                    .build());
            aggregationsPipeline.add(Aggregation.group("year", "month", "site").sum("toBeSummed").as("value"));

            result = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                    entity.pitagoraName(), ValueGroupByDTO.class).getMappedResults().stream().map(obj -> new ValueGroupBySiteMonthAndYearDTO((String) obj.get_id().get("site"), Integer.toString((Integer) obj.get_id().get("year")), Integer.toString((Integer) obj.get_id().get("month")), obj.getValue())).collect(Collectors.toList());
        } else {
            aggregationsPipeline.add(Aggregation.group("site").sum("toBeSummed").as("value"));

            result = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                    entity.pitagoraName(), SiteAndExpensesDTO.class).getMappedResults().stream().map(obj -> new ValueGroupBySiteMonthAndYearDTO(obj.get_id(), obj.getValue())).collect(Collectors.toList());
        }

        return result;
    }

    private HistogramChartDTO _getExpensesStats(LinkedHashMap<String, LinkedHashMap<String, Double>> stackedValues) {
        Map<String, Double> categories = new HashMap<>();
        stackedValues.forEach((year, values) -> {
            values.forEach((category, value) -> {
                if (category.equals(TOTAL_EXPENSES_FIELD)) {
                    return;
                }
                if (!categories.containsKey(category)) {
                    categories.put(category, 0D);
                }
                categories.put(category, categories.get(category) + value);
            });
        });
        List<HistogramCategoryDTO> categoriesResult = new ArrayList<>();
        categories.forEach((category, value) -> categoriesResult.add(new HistogramCategoryDTO(category, value)));

        return new HistogramChartDTO(categoriesResult);
    }

    private StackedHistogramChartDTO _getExpensesItemAnalysis(MaintenanceRequestDTO dto) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> result = new LinkedHashMap<>();
        List<HistogramCategoryDTO> electricities = new ArrayList<>(((HistogramChartDTO) sp.thmDireEnergyService.getElectricityTab(dto).getExpensesByYears().getContent()).getCategories());
        List<HistogramCategoryDTO> gas = new ArrayList<>(((HistogramChartDTO) sp.thmDireEnergyService.getGasTab(dto).getExpensesByYears().getContent()).getCategories());

        List<HistogramCategoryDTO> maintenance = new ArrayList<>();
        ((StackedHistogramChartDTO) sp.thmDireMaintenanceService.getReactiveMaintenance(dto).getExpensesByYearAndCategory().getContent()).getStackedValues().forEach((year, map) -> {
            AtomicReference<Double> sumCategoriesValues = new AtomicReference<>(0D);
            map.values().forEach(value -> sumCategoriesValues.set(sumCategoriesValues.get() + value / THOUSANDS_CONVERTER));
            maintenance.add(new HistogramCategoryDTO(year, sumCategoriesValues.get()));
        });

        dto.getYears().forEach(year -> {
            String stringYear = year.toString();
            Double electricitiesValueCurrentYear = electricities.stream().filter(cat -> cat.getCategory().equals(stringYear)).map(HistogramCategoryDTO::getCount).findFirst().orElse(0D);
            Double gasValueCurrentYear = gas.stream().filter(cat -> cat.getCategory().equals(stringYear)).map(HistogramCategoryDTO::getCount).findFirst().orElse(0D);
            Double maintenanceValueCurrentYear = maintenance.stream().filter(cat -> cat.getCategory().equals(stringYear)).map(HistogramCategoryDTO::getCount).findFirst().orElse(0D);
            Double total = electricitiesValueCurrentYear + gasValueCurrentYear + maintenanceValueCurrentYear;

            if (total != 0) {
                result.put(stringYear, new LinkedHashMap());
                result.get(stringYear).putAll(Map.of(
                        "Spese Energia I.I", electricitiesValueCurrentYear,
                        "Spese Gas I.I", gasValueCurrentYear,
                        "Spese Manutenzione", maintenanceValueCurrentYear,
                        TOTAL_EXPENSES_FIELD, total));
            }
        });

        StackedHistogramChartDTO expenseItemAnalysis = new StackedHistogramChartDTO();
        expenseItemAnalysis.setFirstField(ModelField.builder().type(FieldType.STRING).build());
        expenseItemAnalysis.setSecondField(ModelField.builder().type(FieldType.STRING).build());
        expenseItemAnalysis.setStackedValues(result);
        return expenseItemAnalysis;

    }

    private HistogramChartDTO _getTotalExpensesByYears(LinkedHashMap<String, LinkedHashMap<String, Double>> stackedValues) {
        HistogramChartDTO totalExpensesByYears = new HistogramChartDTO();
        totalExpensesByYears.setField(ModelField.builder().type(FieldType.STRING).build());

        List<HistogramCategoryDTO> categories = new ArrayList<>();
        List<Integer> years = stackedValues.keySet().stream().map(Integer::valueOf).collect(Collectors.toList());

        for (int i = 0; i < years.size(); i++) {
            double variation = 100;
            String currentYear = Integer.toString(years.get(i));
            if (i != 0) {
                int previousYear = years.get(i - 1);
                Double previousYearExpenses = stackedValues.get(Integer.toString(previousYear)).get(TOTAL_EXPENSES_FIELD);
                Double currentYearExpenses = stackedValues.get(currentYear).get(TOTAL_EXPENSES_FIELD);
                variation = _calcVariation(currentYearExpenses, previousYearExpenses);
            }
            categories.add(new HistogramCategoryDTO(Integer.toString(years.get(i)), stackedValues.get(currentYear).get(TOTAL_EXPENSES_FIELD), variation));
        }

        totalExpensesByYears.setCategories(categories);

        return totalExpensesByYears;
    }

    private Double _getCurrentYearTotalExpenses(MaintenanceRequestDTO dto) {
        MaintenanceRequestDTO newDTO = new MaintenanceRequestDTO(List.of(Integer.valueOf(new SimpleDateFormat("yyyy").format(new Date()))), dto.getAddresses());
        HistogramCategoryDTO electricitiesCurrentYear = new ArrayList<>(((HistogramChartDTO) sp.thmDireEnergyService.getElectricityTab(newDTO).getExpensesByYears().getContent()).getCategories()).stream().findFirst().orElse(new HistogramCategoryDTO(CURRENT_YEAR_CATEGORY, 0D)); //IN MILIONI
        HistogramCategoryDTO gasCurrentYear = new ArrayList<>(((HistogramChartDTO) sp.thmDireEnergyService.getGasTab(newDTO).getExpensesByYears().getContent()).getCategories()).stream().findFirst().orElse(new HistogramCategoryDTO(CURRENT_YEAR_CATEGORY, 0D)); //IN MILIONI

        AtomicReference<Double> maintenanceCurrentYear = new AtomicReference<>(0D);
        ((StackedHistogramChartDTO) sp.thmDireMaintenanceService.getReactiveMaintenance(newDTO).getExpensesByYearAndCategory().getContent()).getStackedValues().forEach((year, map) -> {
            AtomicReference<Double> sumCategoriesValues = new AtomicReference<>(0D);
            map.values().forEach(value -> sumCategoriesValues.set(sumCategoriesValues.get() + value));
            maintenanceCurrentYear.set(sumCategoriesValues.get());
        });

        return THOUSANDS_CONVERTER * (electricitiesCurrentYear.getCount() + gasCurrentYear.getCount()) + maintenanceCurrentYear.get();
    }

    private StackedHistogramChartDTO _getTotalExpensesByMonth(MaintenanceRequestDTO dto) {
        LinkedHashMap<String, LinkedHashMap<String, Double>> result = new LinkedHashMap<>();
        LinkedHashMap<String, LinkedHashMap<String, Double>> electricitiesExpensesByMonthAndYear = new LinkedHashMap<>(((StackedHistogramChartDTO) sp.thmDireEnergyService.getElectricityTab(dto).getExpensesByMonth().getContent()).getStackedValues());
        LinkedHashMap<String, LinkedHashMap<String, Double>> gasExpensesByMonthAndYear = new LinkedHashMap<>(((StackedHistogramChartDTO) sp.thmDireEnergyService.getGasTab(dto).getExpensesByMonth().getContent()).getStackedValues());

        List<AggregationOperation> addressesYearsFilter = sp.direQueryUtils
                .buildCommonYearsAndAddressesFilters(dto, List.of("reviewedAt"));

        LinkedHashMap<String, LinkedHashMap<String, Double>> reactiveAccountingExpensesByMonthAndYear = new LinkedHashMap<>(sp.direQueryUtils.sumFieldByYearAndMonth(addressesYearsFilter, "costi", "reviewedAt", ThmDireEntity.REACTIVE_ACCOUNTING).getStackedValues());


        electricitiesExpensesByMonthAndYear.keySet().forEach(month -> {
            LinkedHashMap<String, Double> electricitiesCurrentMonth = electricitiesExpensesByMonthAndYear.get(month);
            LinkedHashMap<String, Double> gasCurrentMonth = gasExpensesByMonthAndYear.get(month);
            LinkedHashMap<String, Double> reactiveAccountingMonth = reactiveAccountingExpensesByMonthAndYear.get(month);
            result.put(month, new LinkedHashMap<>());

            dto.getYears().forEach(year -> {
                String stringYear = year.toString();
                Double electricitiesValueCurrentMonthAndYear = Optional.ofNullable(electricitiesCurrentMonth.get(stringYear)).orElse(0D);
                Double gasValueCurrentMonthAndYear = Optional.ofNullable(gasCurrentMonth.get(stringYear)).orElse(0D);
                Double reactiveAccountingValueCurrentMonthAndYear = Optional.ofNullable(reactiveAccountingMonth.get(stringYear)).orElse(0D);

                double totalValueCurrentMonthAndYear = electricitiesValueCurrentMonthAndYear + gasValueCurrentMonthAndYear + (reactiveAccountingValueCurrentMonthAndYear / THOUSANDS_CONVERTER);
                if (totalValueCurrentMonthAndYear != 0D) {
                    result.get(month).put(stringYear, totalValueCurrentMonthAndYear);
                }
            });
        });

        Double currentYearExpensesDecreaseOnSamePeriodOfPreviousYear = _calcCurrentYearExpensesDecreaseOnSamePeriodOfPreviousYear(electricitiesExpensesByMonthAndYear, gasExpensesByMonthAndYear, reactiveAccountingExpensesByMonthAndYear, dto);

        StackedHistogramChartDTO totalExpensesByMonth = new StackedHistogramChartDTO();
        totalExpensesByMonth.setFirstField(ModelField.builder().type(FieldType.STRING).build());
        totalExpensesByMonth.setSecondField(ModelField.builder().type(FieldType.STRING).build());
        totalExpensesByMonth.setCurrentYearVariationComparedToPreviousYear(currentYearExpensesDecreaseOnSamePeriodOfPreviousYear);
        totalExpensesByMonth.setStackedValues(result);
        totalExpensesByMonth.setTotal(_getCurrentYearTotalExpenses(dto));

        return totalExpensesByMonth;
    }

    private Double _calcCurrentYearExpensesDecreaseOnSamePeriodOfPreviousYear(LinkedHashMap<String, LinkedHashMap<String, Double>> electricitiesExpensesByMonthAndYear, LinkedHashMap<String, LinkedHashMap<String, Double>> gasExpensesByMonthAndYear, LinkedHashMap<String,
            LinkedHashMap<String, Double>> reactiveAccountingExpensesByMonthAndYear, MaintenanceRequestDTO dto) {

        String currentYear = new SimpleDateFormat("yyyy").format(new Date());
        String previusYear = String.valueOf(Integer.parseInt(currentYear) - 1);
        Map<String, Double> currentAndPreviusYearTotalExpenses = new HashMap<>(Map.of(currentYear, 0D, previusYear, 0D));
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        //if we do not have data for the current year
        if (!dto.getYears().contains(Integer.valueOf(currentYear)) || !dto.getYears().contains(Integer.valueOf(previusYear))) {
            MaintenanceRequestDTO newDTO = new MaintenanceRequestDTO(List.of(Integer.valueOf(currentYear), Integer.valueOf(previusYear)), dto.getAddresses());

            electricitiesExpensesByMonthAndYear = new LinkedHashMap<>(((StackedHistogramChartDTO) sp.thmDireEnergyService.getElectricityTab(newDTO).getExpensesByMonth().getContent()).getStackedValues());
            gasExpensesByMonthAndYear = new LinkedHashMap<>(((StackedHistogramChartDTO) sp.thmDireEnergyService.getGasTab(newDTO).getExpensesByMonth().getContent()).getStackedValues());

            List<AggregationOperation> addressesYearsFilter = sp.direQueryUtils
                    .buildCommonYearsAndAddressesFilters(newDTO, List.of("reviewedAt"));

            reactiveAccountingExpensesByMonthAndYear = new LinkedHashMap<>(sp.direQueryUtils.sumFieldByYearAndMonth(addressesYearsFilter, "costi", "reviewedAt", ThmDireEntity.REACTIVE_ACCOUNTING).getStackedValues());
        }

        int currentMonth = cal.get(Calendar.MONTH) + 1; //Month start from number 0
        for (int i = 1; i <= currentMonth; i++) {
            String currentMonthName = DireQueryUtils.getMonthName(i, Locale.ITALY);

            LinkedHashMap<String, Double> electricitiesCurrentMonth = electricitiesExpensesByMonthAndYear.get(currentMonthName);
            LinkedHashMap<String, Double> gasCurrentMonth = gasExpensesByMonthAndYear.get(currentMonthName);
            LinkedHashMap<String, Double> reactiveAccountingMonth = reactiveAccountingExpensesByMonthAndYear.get(currentMonthName);

            currentAndPreviusYearTotalExpenses.keySet().forEach(year -> {
                Double electricitiesValueCurrentMonthAndYear = Optional.ofNullable(electricitiesCurrentMonth.get(year)).orElse(0D);
                Double gasValueCurrentMonthAndYear = Optional.ofNullable(gasCurrentMonth.get(year)).orElse(0D);
                Double reactiveAccountingValueCurrentMonthAndYear = Optional.ofNullable(reactiveAccountingMonth.get(year)).orElse(0D);

                currentAndPreviusYearTotalExpenses.put(year, currentAndPreviusYearTotalExpenses.get(year) + electricitiesValueCurrentMonthAndYear + gasValueCurrentMonthAndYear + (reactiveAccountingValueCurrentMonthAndYear / THOUSANDS_CONVERTER));
            });
        }

        return _calcVariation(currentAndPreviusYearTotalExpenses.get(currentYear), currentAndPreviusYearTotalExpenses.get(previusYear));
    }

    private static Double _calcVariation(Double currentYearExpenses, Double previousYearExpenses) {
        return (currentYearExpenses - previousYearExpenses)
                / previousYearExpenses * 100;
    }

    private static class FinanceHelper {
        private Map<String, Double> siteAndTotalExpenses;
        private FinanceTableSiteExpensesLabelDTO financeTableSiteExpensesLabelDTO;
    }
}
