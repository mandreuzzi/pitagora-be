package it.apeiron.pitagora.core.theorems.dire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BimDetailsResponseDTO {
    private Object mostRecentExecuted;
    private String mostRecentExecutedDate;
}
