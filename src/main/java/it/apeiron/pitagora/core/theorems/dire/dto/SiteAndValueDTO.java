package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteAndValueDTO {
    private String site;
    private Double value;

    public SiteAndValueDTO(Map.Entry<String, Double> siteWithValue) {
        this.site = siteWithValue.getKey();
        this.value = siteWithValue.getValue();
    }
}
