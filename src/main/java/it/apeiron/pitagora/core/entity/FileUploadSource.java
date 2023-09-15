package it.apeiron.pitagora.core.entity;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.FILE_WRONG_ROW_COL_NUM;

import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraFile;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraFileRepository;
import it.apeiron.pitagora.core.util.EncodingUtils;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadSource implements ISource {

    private ObjectId fileId;
    private int sheet;
    private int startingRow;
    private int endingRow;
    private int startingColumn;
    private int endingColumn;

    public FileUploadSource(AbstractRecordDTO a, SourceChannel channel, MongoRepository repository) {
        FileUploadSourceDTO d = (FileUploadSourceDTO) a;
        if (StringUtils.isEmpty(d.getContentType())) {
            return;
        }
        update(a, channel, repository);
    }

    @Override
    public void update(AbstractRecordDTO a, SourceChannel channel, MongoRepository repository) {
        FileUploadSourceDTO d = (FileUploadSourceDTO) a;

        if (d.getFileId() == null) {
            String hash = EncodingUtils.calcHash(d.getDataBase64());
            PitagoraFileRepository fileRepo = (PitagoraFileRepository) repository;
            Optional<PitagoraFile> found = fileRepo.findByHashCode(hash);
            if (found.isPresent()) {
                fileId = new ObjectId(found.get().getId());
            } else {
                PitagoraFile file = fileRepo.save(PitagoraFile.create(d, hash));
                fileId = new ObjectId(file.getId());
            }
        } else {
            fileId = new ObjectId(d.getFileId());
        }

        sheet = SourceChannel.FILE_EXCEL.equals(channel) ? d.getSheet() - 1 : 0;
        startingRow = d.getStartingRow() - 1;
        endingRow = d.getEndingRow() - 1;
        startingColumn = d.getStartingColumn() - 1;
        endingColumn = d.getEndingColumn() - 1;
        if (startingRow > endingRow || startingColumn > endingColumn) {
            throw PitagoraException
                    .badRequest(t(FILE_WRONG_ROW_COL_NUM));
        }
    }
}
