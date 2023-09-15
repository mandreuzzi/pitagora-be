package it.apeiron.pitagora.core.theorems.dire.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SustainabilityConsumptionSiteAndYearDTO {

    private Double consumption;
    private String site;
    private int year;

}
