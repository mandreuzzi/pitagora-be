package it.apeiron.pitagora.core.entity.collection;

import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("file")
public class PitagoraFile {

    @Id
    private String id;
    @CreatedDate
    LocalDateTime createdAt;
    private String fileName;
    private String dataBase64;
    private String contentType;
    private String hashCode;
    private Binary dataBinary;


    public static PitagoraFile create(FileUploadSourceDTO d, String hashCode) {
        PitagoraFile pitagoraFile = _create(d, hashCode);
        pitagoraFile.setDataBase64(d.getDataBase64());
        return pitagoraFile;
    }

    public static PitagoraFile create(FileUploadSourceDTO d, Binary binaryData, String hashCode) {
        PitagoraFile pitagoraFile = _create(d, hashCode);
        pitagoraFile.setDataBinary(binaryData);
        return pitagoraFile;
    }

    private static PitagoraFile _create(FileUploadSourceDTO d, String hashCode) {
        return PitagoraFile.builder()
                .fileName(d.getFileName().trim())
                .contentType(d.getContentType())
                .hashCode(hashCode)
                .build();
    }
}
