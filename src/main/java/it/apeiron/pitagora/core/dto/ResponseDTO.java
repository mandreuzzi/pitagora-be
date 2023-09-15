package it.apeiron.pitagora.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private Object data;
    private String message;

    public static ResponseEntity<ResponseDTO> ok(Object body) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .data(body).build());
    }

    public static ResponseEntity<ResponseDTO> ok(Object body, String message) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .data(body)
                .message(message)
                .build());
    }

    public static ResponseEntity<ResponseDTO> created() {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public static ResponseEntity<ResponseDTO> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.builder().message(message).build());
    }
}
