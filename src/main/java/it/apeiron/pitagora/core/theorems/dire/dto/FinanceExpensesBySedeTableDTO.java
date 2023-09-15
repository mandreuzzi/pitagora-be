package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceExpensesBySedeTableDTO {
    private List<SedeExpensesInfoDTO> siteExpensesInfo;
}
