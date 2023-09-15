package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SedeExpensesInfoDTO {

    private String site;
    private double electricityExpenses;
    private double gasExpenses;
    private double maintenanceExpenses;
    private double totalExpenses;
}
