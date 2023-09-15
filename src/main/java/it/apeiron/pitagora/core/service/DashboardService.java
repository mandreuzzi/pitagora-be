package it.apeiron.pitagora.core.service;

import it.apeiron.pitagora.core.dto.AddChartWidgetRequestDTO;
import it.apeiron.pitagora.core.dto.DashboardDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.dto.WidgetDTO.ChartWidgetDTO;
import it.apeiron.pitagora.core.entity.PitagoraDashboard;
import it.apeiron.pitagora.core.entity.Widget.ChartWidget;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@CommonsLog
@RequiredArgsConstructor
@Service
public class DashboardService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
        _init();
    }

    private void _init() {
        if (dashboardRepository.findAll().isEmpty()) {
            dashboardRepository.save(PitagoraDashboard.builder()
                    .name("Dashboard")
                    .widgets(Collections.emptyList()).build());
        }
    }

    private final PitagoraDashboardRepository dashboardRepository;

    public List<ValueDescriptionDTO> findAll() {
        return dashboardRepository.findAll().stream()
                .map(ValueDescriptionDTO::fromResource).collect(Collectors.toList());
    }

    private PitagoraDashboard _getDashboardById(ObjectId dashboardId) {
        return dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> PitagoraException.notAcceptable("Dashboard with id '" + dashboardId + "' not found"));
    }

    public DashboardDTO getWidgetsByDashboardId(ObjectId dashboardId) {
        return _getDashboardId(dashboardId, true);
    }

    public DashboardDTO getDashboardById(ObjectId dashboardId) {
        return _getDashboardId(dashboardId, false);
    }

    private DashboardDTO _getDashboardId(ObjectId dashboardId, boolean calcCharts) {
        PitagoraDashboard dashboard = _getDashboardById(dashboardId);
        DashboardDTO dto = new DashboardDTO(dashboard);
        dto.setWidgets(dashboard.getWidgets().stream()
                .map(_widget ->
                {
                    ChartWidget widget = (ChartWidget) _widget;

                    PitagoraDataset ds = sp.datasetService.findDatasetByIdWithAggregations(widget.getQuery().getDatasetId());
                    String descr = ds.getModel().getStructure().get(widget.getColumnNameX()).getDescription();
                    if (StringUtils.isNotEmpty(widget.getColumnNameY())) {
                        descr += " - " + ds.getModel().getStructure().get(widget.getColumnNameY()).getDescription();
                    }

                    ChartWidgetDTO widgetDto = ChartWidgetDTO.builder()
                            .datasetName(ds.getNameForClient())
                            .description(descr)
                            .query(widget.getQuery())
                            .columnNameX(widget.getColumnNameX())
                            .columnNameY(widget.getColumnNameY())
                            .size(widget.getSize())
                            .build();
                    if (calcCharts) {
                        widgetDto.setChart(sp.chartsService.getChart(widget));
                    }
                    return widgetDto;
                })
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public void create(DashboardDTO dto) {
        _checkModelNameForCreation(dto.getName());
        dashboardRepository.save(new PitagoraDashboard(dto));
        log.info("Dashboard " + dto.getName() + " created");
    }

    @Transactional
    public void update(DashboardDTO dto) {
        PitagoraDashboard dashboard = _getDashboardById(new ObjectId(dto.getId()));
        if (dashboardRepository.existsByName(dto.getName()) && !dto.getName().equals(dashboard.getName())) {
            throw PitagoraException.nameNotAvailable();
        }
        dashboard.update(dto);
        dashboardRepository.save(dashboard);
        log.info("Dashboard " + dto.getName() + " updated");
    }

    @Transactional
    public void delete(ObjectId dashboardId) {
        dashboardRepository.deleteById(dashboardId);
    }

    private void _checkModelNameForCreation(String name) {
        if (dashboardRepository.existsByName(name)) {
            throw PitagoraException.nameNotAvailable();
        }
    }

    @Transactional
    public void addWidget(AddChartWidgetRequestDTO dto) {
        PitagoraDashboard dashboard = _getDashboardById(new ObjectId(dto.getDashboardId()));
        dashboard.getWidgets().add(ChartWidget.builder()
                .query(dto.getQuery())
                .columnNameX(dto.getColumnNameX())
                .columnNameY(dto.getColumnNameY())
                .size(dto.getSize())
                .build());
        dashboardRepository.save(dashboard);
    }
}
