package it.apeiron.pitagora.core.entity.collection;

import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.util.EncodingUtils;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("http_response")
public class PitagoraHttpResponse {

    @Id
    private ObjectId id;
    @CreatedDate
    LocalDateTime createdAt;
    private ObjectId sourceId;
    private int statusCode;
    private String dataBase64;

    public static PitagoraHttpResponse create(HttpResponseDTO dto, ObjectId sourceId) {
        return PitagoraHttpResponse.builder()
                .sourceId(sourceId)
                .statusCode(dto.getStatusCode())
                .dataBase64(EncodingUtils.encodeBase64(dto.getResponseBody()))
                .build();
    }
}
