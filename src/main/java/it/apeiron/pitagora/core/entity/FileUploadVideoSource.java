package it.apeiron.pitagora.core.entity;

import com.mongodb.client.gridfs.model.GridFSFile;
import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.service.BigFileService;
import it.apeiron.pitagora.core.util.EncodingUtils;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadVideoSource implements ISource {

    private ObjectId fileId;

    public FileUploadVideoSource(FileUploadSourceDTO d, BigFileService bigFileService) {
        if (StringUtils.isEmpty(d.getContentType())) {
            return;
        }
        update(d, bigFileService);
    }

    @SneakyThrows
    public void update(FileUploadSourceDTO d, BigFileService bigFileService) {
        if (d.getFileId() == null) {
            byte[] file = EncodingUtils.base64toByteArray(d.getDataBase64());
            String hash = EncodingUtils.calcHash(file);
            Optional<GridFSFile> found = bigFileService.findByHashCode(hash);
            fileId = found.map(GridFSFile::getObjectId)
                    .orElseGet(() -> bigFileService.addVideo(new ByteArrayInputStream(file), d, hash));
        } else {
            fileId = new ObjectId(d.getFileId());
        }

    }

    @Override
    public void update(AbstractRecordDTO a, SourceChannel channel, MongoRepository repository) {
    }
}
