package it.apeiron.pitagora.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AlarmsSeverityInfoDTO {
    private int countHigh;
    private int countMedium;
    private int countLow;
}
