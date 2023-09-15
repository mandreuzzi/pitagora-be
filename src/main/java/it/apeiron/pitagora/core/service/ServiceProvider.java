package it.apeiron.pitagora.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.apeiron.pitagora.core.quartz.JobService;
import it.apeiron.pitagora.core.service.auth.JwtService;
import it.apeiron.pitagora.core.service.auth.UserService;
import it.apeiron.pitagora.core.theorems.dire.DireQueryUtils;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireBimService;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireEnergyService;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireExportService;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireFinanceService;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireMaintenanceService;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireService;
import it.apeiron.pitagora.core.theorems.dire.service.ThmDireSustainabilityService;
import it.apeiron.pitagora.core.theorems.logico.ThmLogicoService;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxService;
import it.apeiron.pitagora.core.theorems.lux.service.ThmLuxDeviceService;
import it.apeiron.pitagora.core.theorems.vedo.ThmVedoService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@CommonsLog
@Service
@RequiredArgsConstructor
public class ServiceProvider {

    public final SourceService sourceService;
    public final ExcelService excelService;
    public final CSVService csvService;
    public final HttpService httpService;
    public final JsonService jsonService;
    public final JobService jobService;
    public final ModelService modelService;
    public final MapperService mapperService;
    public final DatasetService datasetService;
    public final DataRecordsService dataRecordsService;
    public final AlarmService alarmService;
    public final AlarmEventService alarmEventService;
    public final StatisticsService statisticsService;
    public final ChartsService chartsService;
    public final NotificationService notificationService;
    public final DashboardService dashboardService;
    public final VideoService videoService;
    public final BigFileService bigFileService;
    public final UserService userService;
    public final JwtService jwtService;
    public final TestService testService;
    public final ThmVedoService thmVedoService;
    public final ThmLuxService thmLuxService;
    public final ThmLuxDeviceService thmLuxDeviceService;
    public final ThmLogicoService thmLogicoService;
    public final ThmDireService thmDireService;
    public final DireQueryUtils direQueryUtils;
    public final ThmDireMaintenanceService thmDireMaintenanceService;
    public final ThmDireEnergyService thmDireEnergyService;
    public final ThmDireSustainabilityService thmDireSustainabilityService;
    public final ThmDireFinanceService thmDireFinanceService;
    public final ThmDireBimService thmDireBimService;
    public final ThmDireExportService thmDireExportService;

    @Autowired
    public MongoTemplate mongoTemplate;
    @Autowired
    @Qualifier(value = "mongoTemplateData")
    public MongoTemplate mongoTemplateData;

    public final ObjectMapper om = new ObjectMapper();

    @PostConstruct
    public void init() {
        sourceService.setSp(this);
        excelService.setSp(this);
        csvService.setSp(this);
        httpService.setSp(this);
        jsonService.setSp(this);
        jobService.setSp(this);
        modelService.setSp(this);
        mapperService.setSp(this);
        datasetService.setSp(this);
        dataRecordsService.setSp(this);
        alarmService.setSp(this);
        alarmEventService.setSp(this);
        statisticsService.setSp(this);
        chartsService.setSp(this);
        notificationService.setSp(this);
        dashboardService.setSp(this);
        videoService.setSp(this);
        bigFileService.setSp(this);
        userService.setSp(this);
        jwtService.setSp(this);
        testService.setSp(this);
        thmVedoService.setSp(this);
        thmLuxService.setSp(this);
        thmLuxDeviceService.setSp(this);
        thmLogicoService.setSp(this);
        thmDireService.setSp(this);
        direQueryUtils.setSp(this);
        thmDireMaintenanceService.setSp(this);
        thmDireEnergyService.setSp(this);
        thmDireSustainabilityService.setSp(this);
        thmDireFinanceService.setSp(this);
        thmDireBimService.setSp(this);
        thmDireExportService.setSp(this);
    }
}
