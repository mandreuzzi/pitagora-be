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
public class FinanceTabDTO {
    private DireTabItemDTO totalExpensesByYears;
    private DireTabItemDTO totalExpensesByMonth;
    private DireTabItemDTO expenseItemAnalysis;
    private DireTabItemDTO expensesStats;
    private DireTabItemDTO avgExpensesBySite;
    private DireTabItemDTO expensesBySiteStats;
    private DireTabItemDTO siteExpensesStatisticsLabel;
    private List<SiteDTO> totalExpensesBySiteOnMap;

    public static FinanceTabDTO buildWithDefaultPreferences() {
        int i = -1;
        return FinanceTabDTO.builder()
                .siteExpensesStatisticsLabel(new DireTabItemDTO(++i, "siteExpensesStatisticsLabel"))
                .totalExpensesByYears(new DireTabItemDTO(++i, "totalExpensesByYears"))
                .totalExpensesByMonth(new DireTabItemDTO(++i, "totalExpensesByMonth"))
                .expenseItemAnalysis(new DireTabItemDTO(++i, "expenseItemAnalysis"))
                .expensesStats(new DireTabItemDTO(++i, "expensesStats"))
                .avgExpensesBySite(new DireTabItemDTO(++i, "avgExpensesBySite"))
                .expensesBySiteStats(new DireTabItemDTO(++i, "expensesBySiteStats"))
                .build();
    }
}
