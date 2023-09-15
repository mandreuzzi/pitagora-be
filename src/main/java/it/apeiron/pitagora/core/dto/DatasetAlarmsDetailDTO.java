package it.apeiron.pitagora.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetAlarmsDetailDTO {

    private String id;
    private String name;
    private String notification;
    private String enabledOnTotal;
    private Long lastEvent;

    public DatasetAlarmsDetailDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
