package it.apeiron.pitagora.core.dto;


import it.apeiron.pitagora.core.entity.collection.PitagoraFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {
    private String fileName;
    private String dataBase64;
    private String contentType;

    public static FileDTO fromModel(PitagoraFile file) {
        return FileDTO.builder()
                .fileName(file.getFileName())
                .dataBase64(file.getDataBase64())
                .contentType(file.getContentType())
                .build();
    }
}
