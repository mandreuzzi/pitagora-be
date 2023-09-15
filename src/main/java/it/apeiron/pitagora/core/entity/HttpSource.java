package it.apeiron.pitagora.core.entity;

import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.dto.HttpSourceDTO;
import it.apeiron.pitagora.core.entity.enums.DataUpdateRateUnit;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.entity.enums.UpdateRateMode;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpSource implements ISource {

    private String url;
    private HttpMethod method;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Object body;
    private UpdateRateMode updateRateMode;
    private int updateRateValue;
    private DataUpdateRateUnit updateRateUnit;
    private ObjectId sampleResponseId;
    private ObjectId scheduledJobId;

    public HttpSource(AbstractRecordDTO d) {
        update(d, SourceChannel.HTTP, null);
    }

    public void update(AbstractRecordDTO a, SourceChannel channel, MongoRepository repository) {
        HttpSourceDTO d = (HttpSourceDTO) a;
        url = d.getUrl().trim();
        method = d.getMethod();
        body = d.getBody();
        updateRateMode = d.getUpdateRateMode();
        updateRateValue = d.getUpdateRateValue();
        updateRateUnit = d.getUpdateRateUnit();
        headers = new HashMap<>();
        params = new HashMap<>();
        d.getHeaders().forEach(h -> headers.put(h.getKey().trim(), h.getValue().trim()));
        d.getParams().forEach(p -> params.put(p.getKey().trim(), p.getValue().trim()));

    }
}
