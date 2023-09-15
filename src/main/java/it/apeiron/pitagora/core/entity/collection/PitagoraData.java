package it.apeiron.pitagora.core.entity.collection;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PitagoraData {

    @Id
    private String id;
    @CreatedDate
    private LocalDateTime createdAt;
    private Map<String, Object> data = new HashMap<>();
    private List<PitagoraData> aggregations;

    public PitagoraData(Map<String, Object> data) {
        this.data = data;
    }
}
