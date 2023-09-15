package it.apeiron.pitagora.core.theorems.dire.service;

import static org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToDate.toDate;
import static org.springframework.data.mongodb.core.aggregation.DateOperators.Year.year;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.dire.DireUserPreferences;
import it.apeiron.pitagora.core.theorems.dire.ThmDireEntity;
import it.apeiron.pitagora.core.theorems.dire.dto.AllYearsAndSiteDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.FinanceTabDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.ReactiveMaintenanceDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.ScheduledMaintenanceDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SiteDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SustainabilityTabDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.CategoryValueDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.EnergyTabDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation.AddFieldsOperationBuilder;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmDireService {

    private ObjectMapper om = new ObjectMapper();
    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
        _init();
    }

    private void _init() {
        Arrays.stream(ThmDireEntity.values())
                .filter(entity ->
                        sp.datasetService.findByName(entity.pitagoraName()).isEmpty())
                .forEach(entity ->
                        entity.install(sp)
                );
    }

    public AllYearsAndSiteDTO getAllYearsAndAddresses() {
        Set<Integer> years = new HashSet<>();

        List<SiteDTO> allMappedSites = new ArrayList<>(sp.mongoTemplateData.aggregate(
                Aggregation.newAggregation(Aggregation.project()
                        .and("data.sede").as("sede")
                        .and("data.description").as("description")
                        .and("data.latitude").as("latitude")
                        .and("data.longitude").as("longitude")
                ), ThmDireEntity.SITE.pitagoraName(), SiteDTO.class
        ).getMappedResults());

        Arrays.stream(ThmDireEntity.values())
                .filter(dataset -> !ThmDireEntity.SITE.equals(dataset))
                .forEach(dataset -> {
                    _getAllYearsInDataset(dataset.pitagoraName(), years);
                    _getAllSitesInDataset(dataset.pitagoraName(), allMappedSites);
                });

        return AllYearsAndSiteDTO.builder()
                .years(new ArrayList<>(years).stream().sorted().collect(Collectors.toList()))
                .addresses(allMappedSites)
                .build();
    }

    private void _getAllYearsInDataset(String dataset, Set<Integer> years) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>();

        List<String> timestampFields = sp.modelService.findByName(dataset).get()
                .getStructure()
                .values().stream()
                .filter(modelField -> FieldType.TIMESTAMP.equals(modelField.getType()))
                .map(ModelField::getName)
                .collect(Collectors.toList());

        aggregationsPipeline.add(Aggregation.match(new Criteria().andOperator(
                timestampFields.stream().map(field -> Criteria.where("data." + field).ne(null)).collect(Collectors.toList())
        )));

        AtomicReference<AddFieldsOperationBuilder> calcYear = new AtomicReference<>(Aggregation.addFields());
        timestampFields.forEach(field -> calcYear.set(calcYear.get()
                .addField("year_" + field).withValue(year(toDate("$data." + field))))
        );
        aggregationsPipeline.add(calcYear.get().build());

        AtomicReference<ProjectionOperation> getYear = new AtomicReference<>(Aggregation.project());
        timestampFields.forEach(field -> getYear.set(getYear.get()
                .and("year_" + field).as("year_" + field))
        );
        aggregationsPipeline.add(getYear.get());

        List<Map> mapsOfYears = sp.mongoTemplateData.aggregate(Aggregation.newAggregation(aggregationsPipeline),
                dataset, Map.class).getMappedResults();
        mapsOfYears.forEach(map -> {
            map.forEach((k, v) -> {
                if (!"_id".equals(k)) {
                    years.add((Integer) v);
                }
            });
        });

    }

    private void _getAllSitesInDataset(String dataset, List<SiteDTO> allMappedSites) {
        List<AggregationOperation> aggregationsPipeline = new ArrayList<>();
        aggregationsPipeline.add(Aggregation.match(Criteria.where("data.sede").ne(null)));
        aggregationsPipeline.add(Aggregation.group("data.sede").count().as("count"));
        aggregationsPipeline.add(Aggregation.project()
                .and("_id").as("category")
        );

        List<CategoryValueDTO> results = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(aggregationsPipeline), dataset,
                        CategoryValueDTO.class)
                .getMappedResults();

        results.forEach(cat -> {
            SiteDTO site = new SiteDTO(cat.getCategory());
            if (!allMappedSites.contains(site)) {
                allMappedSites.add(site);
            }
        });
    }

    public LinkedHashMap<String, LinkedHashMap<String, Double>> sortInnerByValueAndOuterByInner(LinkedHashMap<String, LinkedHashMap<String, Double>> results) {
        List<Entry<String, LinkedHashMap<String, Double>>> entries = new ArrayList<>(results.entrySet());
        entries.sort(Comparator.comparing(entry -> entry.getValue().values().stream().reduce(0.0, Double::sum)));
        LinkedHashMap<String, LinkedHashMap<String, Double>> sorted = new LinkedHashMap<>();
        entries.forEach(entry -> {
            List<Entry<String, Double>> innerEntries = new ArrayList<>(entry.getValue().entrySet());
            innerEntries.sort(Entry.comparingByValue());
            LinkedHashMap<String, Double> innerSorted = new LinkedHashMap<>();
            innerEntries.forEach(innerEntry -> innerSorted.put(innerEntry.getKey(), innerEntry.getValue()));
            sorted.put(entry.getKey(), innerSorted);
        });
        return sorted;
    }

    public void updateTabPreferences(String tab, Object prefs) {
        PitagoraUser loggedUser = sp.jwtService.getLoggedUser();
        DireUserPreferences direUserPreferences = (DireUserPreferences) loggedUser.getPreferences().get(Theorem.DIRE);
        switch (tab) {
            case "finance":
                direUserPreferences.setFinanceTab(om.convertValue(prefs, FinanceTabDTO.class));
                break;
            case "energy-electricity":
                direUserPreferences.setElectricityTab(om.convertValue(prefs, EnergyTabDTO.class));
                break;
            case "energy-gas":
                direUserPreferences.setGasTab(om.convertValue(prefs, EnergyTabDTO.class));
                break;
            case "maintenance-scheduled":
                direUserPreferences.setScheduledTab(om.convertValue(prefs, ScheduledMaintenanceDTO.class));
                break;
            case "maintenance-reactive":
                direUserPreferences.setReactiveTab(om.convertValue(prefs, ReactiveMaintenanceDTO.class));
                break;
            case "sustainability":
                direUserPreferences.setSustainabilityTab(om.convertValue(prefs, SustainabilityTabDTO.class));
                break;
        }
        sp.mongoTemplate.save(loggedUser);
    }
}
