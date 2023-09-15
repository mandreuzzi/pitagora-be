package it.apeiron.pitagora.core.theorems.dire.dto.query;

import it.apeiron.pitagora.core.theorems.dire.dto.DireTabItemDTO;
import it.apeiron.pitagora.core.theorems.dire.dto.SiteDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyTabDTO {
    private DireTabItemDTO consumptionBySite;
    private DireTabItemDTO expensesByMonth;
    private DireTabItemDTO expensesByYears;
    private DireTabItemDTO totalEnergyExpenses;
    private DireTabItemDTO consumptionByMonth;
    private DireTabItemDTO consumptionByYears;
    private DireTabItemDTO expensesHistory;
    private List<SiteDTO> consumptionBySiteOnMap;

    public static EnergyTabDTO buildWithDefaultPreferences() {
        int i = -1;
        return EnergyTabDTO.builder()
                .consumptionBySite(new DireTabItemDTO(++i, "consumptionBySite"))
                .expensesByMonth(new DireTabItemDTO(++i, "expensesByMonth"))
                .expensesByYears(new DireTabItemDTO(++i, "expensesByYears"))
                .totalEnergyExpenses(new DireTabItemDTO(++i, "totalEnergyExpenses"))
                .consumptionByMonth(new DireTabItemDTO(++i, "consumptionByMonth"))
                .consumptionByYears(new DireTabItemDTO(++i, "consumptionByYears"))
                .expensesHistory(new DireTabItemDTO(++i, "expensesHistory"))
                .build();
    }
}
