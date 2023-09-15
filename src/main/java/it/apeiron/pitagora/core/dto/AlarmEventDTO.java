package it.apeiron.pitagora.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmEventDTO extends AbstractRecordDTO {

    private String datasetName;
    private String alarmName;
    private Long eventTime;
    private List<String> triggerValues;
    private String severity;
}
