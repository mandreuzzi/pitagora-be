package it.apeiron.pitagora.core.theorems.dire.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AllYearsAndSiteDTO {
    private List<Integer> years;
    private List<SiteDTO> addresses;
}
