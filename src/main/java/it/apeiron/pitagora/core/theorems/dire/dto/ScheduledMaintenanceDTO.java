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
public class ScheduledMaintenanceDTO {

    private DireTabItemDTO activitiesProgress;
    private DireTabItemDTO progressByBuilding;
    private DireTabItemDTO progressByCategory;
    private DireTabItemDTO delayByCategory;
    private DireTabItemDTO delayByZone;
    private List<SiteDTO> progressBySiteOnMap;

    public static ScheduledMaintenanceDTO buildWithDefaultPreferences() {
        int i = -1;
        return ScheduledMaintenanceDTO.builder()
                .activitiesProgress(new DireTabItemDTO(++i, "activitiesProgress"))
                .progressByBuilding(new DireTabItemDTO(++i, "progressByBuilding"))
                .delayByCategory(new DireTabItemDTO(++i, "delayByCategory"))
                .progressByCategory(new DireTabItemDTO(++i, "progressByCategory"))
                .delayByZone(new DireTabItemDTO(++i, "delayByZone"))
                .build();
    }
}
