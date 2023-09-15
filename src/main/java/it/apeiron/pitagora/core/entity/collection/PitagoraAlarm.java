package it.apeiron.pitagora.core.entity.collection;

import it.apeiron.pitagora.core.dto.AlarmDTO;
import it.apeiron.pitagora.core.entity.enums.AlarmSeverity;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.NotificationChannel;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document("alarm")
public class PitagoraAlarm extends AbstractPitagoraRecord {

    private ObjectId datasetId;
    private NotificationChannel notification;
    private boolean enabled;
    private List<AlarmCondition> conditions;
    private AlarmSeverity severity;

    public PitagoraAlarm(ObjectId datasetId, List<Theorem> scope, AlarmDTO dto) {
        this.datasetId = datasetId;
        this.scope = scope;
        update(dto);
    }

    public void update(AlarmDTO d) {
        superUpdate(d);
        notification = d.getNotification();
        enabled = d.isEnabled();
        conditions = d.getConditions();
        severity = d.getSeverity();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlarmCondition {
        private String field;
        private FieldType fieldType;
        private Operation operation;
        private String value;

        public void setValue(String value) {
            this.value = value.trim();
        }
    }
}
