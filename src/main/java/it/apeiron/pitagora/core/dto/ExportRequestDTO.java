package it.apeiron.pitagora.core.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDTO {
    private String tabCode;
    private String logoBase64;
    private String title;
    private List<String> addresses;
    private List<String> years;
    private List<ExportRequestItem> contents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportRequestItem {
        private String type;
        private Object data;
    }

    @NoArgsConstructor
    @Data
    public static class FinanceTabExportDetail {
        private String siteWithTheLargestIncreaseAnnualExpenses;
        private String siteWithHighestAverageExpensesPerSquareMeter;
        private FinanceOtherStats otherStats;
        private List<Map<String, String>> expensesBySiteStatsTable;

        @NoArgsConstructor
        @Data
        public static class FinanceOtherStats {
            private String minYear;
            private String maxYear;
            private String siteWithMaxExpensesDesc;
            private String siteWithMaxExpensesValue;
            private String siteWithMaxExpensesIncreaseOrDecreaseDesc;
            private String maxIncreaseOrDecrease;
            private String maxIncreaseOrDecreaseValue;
            private String maxOrMinIncreaseOrDecrease;
            private String siteWithMaxOrMinExpensesIncreaseOrDecreaseDesc;
            private String siteWithMaxOrMinExpensesIncreaseOrDecreaseValue;
        }
    }

    @NoArgsConstructor
    @Data
    public static class EnergyTabExportDetail {
        private String value;
        private String description;
        private String labelSuffix;
    }

    @NoArgsConstructor
    @Data
    public static class ReactiveMaintenanceTabExportDetail {
        private String increaseOrDecrease;
        private String increaseOrDecreaseValue;
        private String firstMonth;
        private String firstYear;
        private String lastMonth;
        private String lastYear;
    }

    @NoArgsConstructor
    @Data
    public static class SustainabilityTabExportDetail {
        private String firstAnnualScope;
        private String secondAnnualScope;
        private String firstMonthScope;
        private String secondMonthScope;
    }
}
