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
public class SustainabilityTabDTO {
    private DireTabItemDTO scopes;
    private DireTabItemDTO specificConsumptionBySite;
    private DireTabItemDTO objectiveAnalysis;
    private List<SiteDTO> specificConsumptionBySiteOnMap;

    public static SustainabilityTabDTO buildWithDefaultPreferences() {
        int i = -1;
        return SustainabilityTabDTO.builder()
                .scopes(new DireTabItemDTO(++i, "scopes"))
                .specificConsumptionBySite(new DireTabItemDTO(++i, "specificConsumptionBySite"))
                .objectiveAnalysis(new DireTabItemDTO(++i, "objectiveAnalysis"))
                .build();
    }
}
