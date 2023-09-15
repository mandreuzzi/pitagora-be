package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.entity.FileUploadSource;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileUploadSourceDTO extends AbstractRecordDTO {

    // TODO aggiungere validazioni sui campi
    private String fileId;
    private String fileName;
    private String dataBase64;
    private String contentType;
    private SourceChannel channel;
    private int sheet;
    private int startingRow;
    private int endingRow;
    private int startingColumn;
    private int endingColumn;

    public static FileUploadSourceDTO fromModel(PitagoraSource m) {
        FileUploadSourceDTO dto = new FileUploadSourceDTO();
        FileUploadSource cfg = m.getFileUploadConfiguration();
        dto.setSuperProps(m);
        dto.setFileId(cfg.getFileId().toString());
        dto.setChannel(m.getChannel());
        if (SourceChannel.FILE_EXCEL.equals(m.getChannel())) {
            dto.setSheet(cfg.getSheet() + 1) ;
        }
        dto.setStartingRow(cfg.getStartingRow() + 1) ;
        dto.setEndingRow(cfg.getEndingRow() + 1) ;
        dto.setStartingColumn(cfg.getStartingColumn() + 1) ;
        dto.setEndingColumn(cfg.getEndingColumn() + 1) ;
        return dto;
    }

}
