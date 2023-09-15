package it.apeiron.pitagora.core.entity.collection;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "alarm_event")
public class PitagoraAlarmEvent extends AbstractPitagoraRecord {

    private ObjectId alarmId;
    private ObjectId datasetId;
    private Long eventTime;
    private Map<String, Object> triggerValues;

    // the following lists are used only on aggregations query and, if not null, they contain always only the unique aggregated object
    private List<PitagoraDataset> dataset;
    private List<PitagoraAlarm> alarm;

    public PitagoraDataset getDataset() {
        return dataset != null && !dataset.isEmpty() ? dataset.get(0) : null;
    }
    public PitagoraAlarm getAlarm() {
        return alarm != null && !alarm.isEmpty() ? alarm.get(0) : null;
    }
}
