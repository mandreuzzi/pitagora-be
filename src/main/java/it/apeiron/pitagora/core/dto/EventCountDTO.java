package it.apeiron.pitagora.core.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCountDTO {

    private String datasetName;
    private int totalCount;
    private List<ValueDescriptionDTO> alarms = new ArrayList<>();

}
