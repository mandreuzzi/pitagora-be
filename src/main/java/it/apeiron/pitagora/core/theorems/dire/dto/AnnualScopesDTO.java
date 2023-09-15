package it.apeiron.pitagora.core.theorems.dire.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnualScopesDTO {
    private Double firstAnnualScope;
    private Double secondAnnualScope;
    private Double firstMonthScope;
    private Double secondMonthScope;
}
