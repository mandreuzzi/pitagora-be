package it.apeiron.pitagora.core.theorems.dire.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceActivityProgressDTO {
    private double total;
    private double completed;
}
