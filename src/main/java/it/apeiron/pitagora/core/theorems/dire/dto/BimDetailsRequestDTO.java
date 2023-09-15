package it.apeiron.pitagora.core.theorems.dire.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BimDetailsRequestDTO {
    private List<Integer> years;
    private String site;
    private String tag;
    private String context;
}
