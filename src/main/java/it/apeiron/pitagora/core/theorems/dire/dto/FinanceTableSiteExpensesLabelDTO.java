package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinanceTableSiteExpensesLabelDTO {
    public static final String ONLY_INCREASE = "Solo Aumenti";
    public static final String INCREASE_AND_DECREASE= "Aumenti e Decrementi";
    public static final String ONLY_DECREASE = "Solo Decrementi";

    private String siteWithTheLargestIncreaseAnnualExpenses;
    private String siteWithHighestAverageExpensesPerSquareMeter;
    private Integer minYear;
    private Integer maxYear;
    private SiteAndValueDTO siteWithMaxExpensesIncreaseOrDecrease;
    private SiteAndValueDTO siteWithMaxOrMinExpensesIncreaseOrDecrease;
    private String increaseOrDecrease;
    private SiteAndValueDTO siteWithMaxExpenses;

    public FinanceTableSiteExpensesLabelDTO(String siteWithTheLargestIncreaseAnnualExpenses, String siteWithHighestAverageExpenditurePerSquareMeter, int minYear, int maxYear) {
        this.siteWithTheLargestIncreaseAnnualExpenses = siteWithTheLargestIncreaseAnnualExpenses;
        this.siteWithHighestAverageExpensesPerSquareMeter = siteWithHighestAverageExpenditurePerSquareMeter;
        this.minYear = minYear;
        this.maxYear = maxYear;
    }
}
