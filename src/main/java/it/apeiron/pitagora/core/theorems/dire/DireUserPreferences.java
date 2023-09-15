package it.apeiron.pitagora.core.theorems.dire;

import it.apeiron.pitagora.core.theorems.dire.dto.FinanceTabDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.ReactiveMaintenanceDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.ScheduledMaintenanceDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SustainabilityTabDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.query.EnergyTabDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireUserPreferences {

    private FinanceTabDTO financeTab;
    private EnergyTabDTO electricityTab;
    private EnergyTabDTO gasTab;
    private ScheduledMaintenanceDTO scheduledTab;
    private ReactiveMaintenanceDTO reactiveTab;
    private SustainabilityTabDTO sustainabilityTab;

    public static DireUserPreferences buildDefault() {
        return DireUserPreferences.builder()
                .financeTab(FinanceTabDTO.buildWithDefaultPreferences())
                .electricityTab(EnergyTabDTO.buildWithDefaultPreferences())
                .gasTab(EnergyTabDTO.buildWithDefaultPreferences())
                .scheduledTab(ScheduledMaintenanceDTO.buildWithDefaultPreferences())
                .reactiveTab(ReactiveMaintenanceDTO.buildWithDefaultPreferences())
                .sustainabilityTab(SustainabilityTabDTO.buildWithDefaultPreferences())
                .build();
    }
}
