package it.apeiron.pitagora.core.theorems.dire.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactiveMaintenanceDTO {

    private DireTabItemDTO monthExpenses;
    private DireTabItemDTO monthExpensesInfo;
    private DireTabItemDTO tickets;
    private DireTabItemDTO expensesByYearAndCategory;
    private DireTabItemDTO expensesBySiteAndCategory;
    private DireTabItemDTO expensesByCategoryAndComponent;
    private DireTabItemDTO expensesByYearAndLocalization;
    private DireTabItemDTO ticketTimeVsExpenses;
    private DireTabItemDTO delayByComponent;
    private List<SiteDTO> expensesBySiteOnMap;

    public static ReactiveMaintenanceDTO buildWithDefaultPreferences() {
        int i = -1;
        return ReactiveMaintenanceDTO.builder()
                .monthExpenses(new DireTabItemDTO(++i, "monthExpenses"))
                .monthExpensesInfo(new DireTabItemDTO(++i, "monthExpensesInfo"))
                .tickets(new DireTabItemDTO(++i, "tickets"))
                .expensesByYearAndCategory(new DireTabItemDTO(++i, "expensesByYearAndCategory"))
                .expensesBySiteAndCategory(new DireTabItemDTO(++i, "expensesBySiteAndCategory"))
                .expensesByCategoryAndComponent(new DireTabItemDTO(++i, "expensesByCategoryAndComponent"))
                .expensesByYearAndLocalization(new DireTabItemDTO(++i, "expensesByYearAndLocalization"))
                .ticketTimeVsExpenses(new DireTabItemDTO(++i, "ticketTimeVsExpenses"))
                .delayByComponent(new DireTabItemDTO(++i, "delayByComponent"))
                .build();
    }
}
