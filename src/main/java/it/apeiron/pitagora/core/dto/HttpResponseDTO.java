package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.collection.PitagoraHttpResponse;
import it.apeiron.pitagora.core.util.EncodingUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class HttpResponseDTO {

    private int statusCode;
    private String responseBody;

    public static HttpResponseDTO fromModel(PitagoraHttpResponse m) {
        return HttpResponseDTO.builder()
                .statusCode(m.getStatusCode())
                .responseBody(EncodingUtils.decodeBase64(m.getDataBase64()))
                .build();
    }

}
