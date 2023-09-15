package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.PitagoraAlarm;
import it.apeiron.pitagora.core.entity.collection.PitagoraAlarm.AlarmCondition;
import it.apeiron.pitagora.core.entity.enums.AlarmSeverity;
import it.apeiron.pitagora.core.entity.enums.NotificationChannel;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AlarmDTO extends AbstractRecordDTO {

    private NotificationChannel notification;
    private boolean enabled;
    private List<AlarmCondition> conditions;
    private List<AlarmEventDTO> events;
    private AlarmSeverity severity;

    public static AlarmDTO fromModel(PitagoraAlarm a) {
        AlarmDTO dto = AlarmDTO.builder()
                .enabled(a.isEnabled())
                .conditions(a.getConditions())
                .notification(a.getNotification())
                .severity(a.getSeverity())
                .build();
        dto.setSuperProps(a);
        return dto;
    }

}
