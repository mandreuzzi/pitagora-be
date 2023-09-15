package it.apeiron.pitagora.core.quartz;

import static it.apeiron.pitagora.core.quartz.JobService.DATASET_ID;
import static it.apeiron.pitagora.core.quartz.JobService.MAPPER_ID;
import static it.apeiron.pitagora.core.quartz.JobService.SOURCE_ID;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.quartz.JobDetail;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("job")
public class PitagoraJob {

    @Id
    private ObjectId id;
    @CreatedDate
    private LocalDateTime createdAt;
    private String jobKey;
    private ObjectId datasetId;
    private ObjectId sourceId;
    private ObjectId mapperId;
//    private boolean active;

    public PitagoraJob(JobDetail job) {
        jobKey = job.getKey().getName();
        datasetId = new ObjectId((String) job.getJobDataMap().get(DATASET_ID));
        sourceId = new ObjectId((String) job.getJobDataMap().get(SOURCE_ID));
        mapperId = new ObjectId((String) job.getJobDataMap().get(MAPPER_ID));
    }
}
