package it.apeiron.pitagora.core.entity.collection;

import static it.apeiron.pitagora.core.entity.enums.SourceChannel.FILE_CSV;
import static it.apeiron.pitagora.core.entity.enums.SourceChannel.FILE_EXCEL;
import static it.apeiron.pitagora.core.entity.enums.SourceChannel.FILE_MP4;

import it.apeiron.pitagora.core.dto.AbstractRecordDTO;
import it.apeiron.pitagora.core.dto.ExposedApiSourceDTO;
import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import it.apeiron.pitagora.core.dto.HttpSourceDTO;
import it.apeiron.pitagora.core.entity.ExposedApiSource;
import it.apeiron.pitagora.core.entity.FileUploadSource;
import it.apeiron.pitagora.core.entity.FileUploadVideoSource;
import it.apeiron.pitagora.core.entity.HttpSource;
import it.apeiron.pitagora.core.entity.ISource;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.service.BigFileService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Data
@NoArgsConstructor
@SuperBuilder
@Document("source")
public class PitagoraSource extends AbstractPitagoraRecord {

    private SourceChannel channel;
    private ISource configuration;

    public PitagoraSource(AbstractRecordDTO d, SourceChannel channel, MongoRepository repository, BigFileService bigFileService) {
        update(d, channel, repository, bigFileService);
    }

    public void update(AbstractRecordDTO d, SourceChannel channel, MongoRepository repository, BigFileService bigFileService) {

        superUpdate(d);
        this.channel = channel;
        if (StringUtils.isNotEmpty(d.getId())) {
            configuration.update(d, channel, repository);
        } else {
            if (d instanceof HttpSourceDTO) {
                configuration = new HttpSource(d);
            } else if (d instanceof FileUploadSourceDTO) {
                if (FILE_EXCEL.equals(channel) || FILE_CSV.equals(channel)) {
                    configuration = new FileUploadSource(d, channel, repository);
                } else if (FILE_MP4.equals(channel)) {
                    configuration = new FileUploadVideoSource((FileUploadSourceDTO) d, bigFileService);
                }
            } else if (d instanceof ExposedApiSourceDTO) {
                configuration = new ExposedApiSource(d, channel, repository);
            }
        }

    }

    public HttpSource getHttpConfiguration() {
        return (HttpSource) configuration;
    }

    public FileUploadSource getFileUploadConfiguration() {
        return (FileUploadSource) configuration;
    }

    public FileUploadVideoSource getFileUploadVideoConfiguration() {
        return (FileUploadVideoSource) configuration;
    }

    public ExposedApiSource getExposedApiConfiguration() {
        return (ExposedApiSource) configuration;
    }
}
