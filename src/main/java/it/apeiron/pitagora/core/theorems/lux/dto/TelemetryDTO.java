package it.apeiron.pitagora.core.theorems.lux.dto;

import java.util.Date;
import lombok.Data;

@Data
public class TelemetryDTO {

    private String name;
    private Date date;
    private Integer lux;
    private Double power;
    private Integer voltage;
    private Integer current;

}
