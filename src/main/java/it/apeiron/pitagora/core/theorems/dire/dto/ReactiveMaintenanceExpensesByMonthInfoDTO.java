package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactiveMaintenanceExpensesByMonthInfoDTO {
    private Double variation;
    private Integer variationFirstYear;
    private Integer variationLastYear;
    private String variationFirstMonth;
    private String variationLastMonth;

}
