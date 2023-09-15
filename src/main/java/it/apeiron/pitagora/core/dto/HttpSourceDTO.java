package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.HttpSource;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.DataUpdateRateUnit;
import it.apeiron.pitagora.core.entity.enums.UpdateRateMode;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HttpSourceDTO extends AbstractRecordDTO {

    // TODO aggiungere validazioni sui campi
    private String url;
    private HttpMethod method;
    private List<KeyValueDTO> headers;
    private List<KeyValueDTO> params;
    private Object body;
    private UpdateRateMode updateRateMode;
    private int updateRateValue;
    private DataUpdateRateUnit updateRateUnit;
    private String sampleResponseId;

    public static HttpSourceDTO fromModel(PitagoraSource m) {
        HttpSource c = m.getHttpConfiguration();
        HttpSourceDTO dto = HttpSourceDTO.builder()
                .url(c.getUrl())
                .method(c.getMethod())
                .headers(new ArrayList<>())
                .params(new ArrayList<>())
                .body(c.getBody())
                .sampleResponseId(c.getSampleResponseId().toString())
                .updateRateMode(c.getUpdateRateMode())
                .updateRateValue(c.getUpdateRateValue())
                .updateRateUnit(c.getUpdateRateUnit())
                .build();
        c.getHeaders().forEach((k, v) -> dto.getHeaders().add(new KeyValueDTO(k,v)));
        c.getParams().forEach((k, v) -> dto.getParams().add(new KeyValueDTO(k,v)));

        dto.setSuperProps(m);
        return dto;
    }
}
