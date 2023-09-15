package it.apeiron.pitagora.core.service;

import it.apeiron.pitagora.core.dto.FileUploadSourceDTO;
import it.apeiron.pitagora.core.util.EncodingUtils;
import it.apeiron.pitagora.core.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.bson.types.Binary;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    public void test() {
    }

    @SneakyThrows
    private void test2(FileUploadSourceDTO srcDto) {

//        byte[] zip = IOUtils.zip(new ByteArrayInputStream(EncodingUtils.base64toByteArray(srcDto.getDataBase64())),srcDto.getName());
//        sp.videoService.addVideo(new ByteArrayInputStream(zip),srcDto);

//        sp.fileService.addVideo(new ByteArrayInputStream(EncodingUtils.base64toByteArray(srcDto.getDataBase64())),srcDto);

    }

    @SneakyThrows
    private void test(String dataBase64) {
        PitTest pitTest = sp.mongoTemplate.find(new Query(), PitTest.class).get(0);
        byte[] file = ((Binary) pitTest.getBody()).getData();
        FileUtils.writeByteArrayToFile(new File("/home/alessandro/Desktop/testZIP/upload.mv4"), file);

//        FileUtils.writeByteArrayToFile(new File("/home/alessandro/Desktop/testZIP/upload.mv4"), EncodingUtils.base64toByteArray(dataBase64));
        String fileToZip = "video999.m4v";
//        File input = new File("/home/alessandro/Desktop/testZIP/" + fileToZip);
//        byte[] zip = IOUtils.zipTest(new FileInputStream(input), fileToZip);
        byte[] zip = IOUtils.zip(new ByteArrayInputStream(EncodingUtils.base64toByteArray(dataBase64)), fileToZip);
//        FileUtils.writeByteArrayToFile(new File("/home/alessandro/Desktop/testZIP/zzz.zip"), zip);

        sp.mongoTemplate.insert(PitTest.create(zip));
        String zipBase64 = Base64.getMimeEncoder().encodeToString(zip);
        byte[] sbasato = Base64.getMimeDecoder().decode(zipBase64);

//        File input = new File("/home/alessandro/Desktop/testZIP/zzz.zip");
//        byte[] unzipped = IOUtils.unzipTest(new FileInputStream(input));
        byte[] unzipped = IOUtils.unzip(new ByteArrayInputStream(sbasato));
        FileUtils.writeByteArrayToFile(new File("/home/alessandro/Desktop/testZIP/upload.mv4"), unzipped);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Document("test")
    public static class PitTest {

        @Id
        private String id;
        @CreatedDate
        LocalDateTime createdAt;
        private String fileName;
        private Object body;

        public static PitTest create(Object body) {
            return PitTest.builder()
                    .body(body)
                    .build();
        }
    }

}
