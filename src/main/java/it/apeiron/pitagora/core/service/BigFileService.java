package it.apeiron.pitagora.core.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.service.VideoService.Streaming;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;


@CommonsLog
@RequiredArgsConstructor
@Service
public class BigFileService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final GridFsTemplate gridFsTemplate;

    private final GridFsOperations operations;

    public ObjectId addVideo(InputStream is, FileUploadSourceDTO file, String hash) {
        DBObject metaData = new BasicDBObject();
        metaData.put("hash", hash);
        metaData.put("type", "video");
        metaData.put("title", file.getName());
        ObjectId id = gridFsTemplate.store(
                is, file.getName(), file.getContentType(), metaData);
        return id;
    }

    public Streaming getVideo(String id) throws IllegalStateException, IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        return Streaming.builder()
                .inputStream(operations.getResource(file).getInputStream())
                .length(file.getLength())
                .build();
    }

    public Optional<GridFSFile> findByHashCode(String hash) {
        return Optional.ofNullable(gridFsTemplate.findOne(new Query(Criteria.where("metadata.hash").is(hash))));
    }

    @SneakyThrows
    public Streaming getVideoBySourceName(String sourceName) {
        PitagoraSource source = sp.sourceService.findByName(sourceName);
        return getVideo(source.getFileUploadVideoConfiguration().getFileId().toString());
    }

    public void deleteById(ObjectId fileId) {
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId.toString())));
    }
}
