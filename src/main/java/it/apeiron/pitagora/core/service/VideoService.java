package it.apeiron.pitagora.core.service;

import it.apeiron.pitagora.core.repository.PitagoraFileRepository;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


@CommonsLog
@RequiredArgsConstructor
@Service
public class VideoService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraFileRepository fileRepository;

    private final Set<String> validTokens = new HashSet<>();

    public String getToken() {
        String token = UUID.randomUUID().toString().substring(0, 16);
        validTokens.add(token);
        return token;
    }

    public boolean streamTokenIsNotValid(String token) {
        return !validTokens.remove(token);
    }

    @SneakyThrows
    public Streaming loadVideo(String videoId) {

        Streaming streaming = sp.bigFileService.getVideoBySourceName("video" + videoId + ".m4v");
        InputStream inputStream = streaming.getInputStream();

        StreamingResponseBody stream = out -> {

            try {

                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) >= 0) {
                    out.write(bytes, 0, length);
                }
                inputStream.close();
                out.flush();

            } catch (final Exception e) {
                log.error("Exception while reading and streaming data {} ", e);
            }
        };

        streaming.setBody(stream);
        return streaming;
    }

//    @SneakyThrows
//    private Streaming getInputStream(String videoId) {
//        PitagoraSource source = sp.sourceService.findByName("video" + videoId + ".m4v");
//        PitagoraFile video = fileRepository.findById(source.getFileUploadVideoConfiguration().getFileId())
//                .orElseThrow(() -> PitagoraException.notAcceptable("File with id '" + source.getFileUploadVideoConfiguration().getFileId() + "' not found"));
//
//        byte[] zip = video.getDataBinary().getData();
//        byte[] bytes = IOUtils.unzip(new ByteArrayInputStream(zip));
//        InputStream inputStream = new ByteArrayInputStream(bytes);
//        return Streaming.builder().inputStream(inputStream).length(bytes.length).build();
//    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Streaming {
        private InputStream inputStream;
        private StreamingResponseBody body;
        private long length;
    }
}
