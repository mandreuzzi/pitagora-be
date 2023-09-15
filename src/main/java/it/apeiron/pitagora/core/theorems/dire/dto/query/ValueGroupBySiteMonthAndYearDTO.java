package it.apeiron.pitagora.core.theorems.dire.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueGroupBySiteMonthAndYearDTO {

    private String site;
    private String year;
    private String month;
    private Double value;

    public ValueGroupBySiteMonthAndYearDTO(String site, Double value){
        this.site = site;
        this.value = value;
    }
}
