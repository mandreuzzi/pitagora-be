package it.apeiron.pitagora.core.theorems.dire.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MaintenanceRequestDTO {
    private List<Integer> years;
    private List<String> addresses;
}
