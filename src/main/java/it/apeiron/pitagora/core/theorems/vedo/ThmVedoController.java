package it.apeiron.pitagora.core.theorems.vedo;

import static it.apeiron.pitagora.core.theorems.vedo.entity.FaceDetectionDataset.FACE_DETECTION_DATASET_NAME;

import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.service.VideoService.Streaming;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@CommonsLog
@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("thm/vedo")
public class ThmVedoController {

    private final ServiceProvider sp;

    @GetMapping("/datasetId")
    public ResponseEntity<ResponseDTO> getSystemDatasetIdByName() {
        return ResponseDTO.ok(sp.datasetService.findByName(FACE_DETECTION_DATASET_NAME).get().getId().toString());
    }

    @GetMapping("/test-dahua")
    public ResponseEntity<ResponseDTO> test() {
        return ResponseDTO.ok(sp.thmVedoService.testDahuaServer());
    }

    @GetMapping("/stream/token")
    public ResponseEntity<ResponseDTO> getStreamToken() {
        return ResponseDTO.ok(sp.videoService.getToken());
    }

    @SneakyThrows
    @GetMapping("/stream/{token}/{videoId}")
    public ResponseEntity<StreamingResponseBody> stream(@PathVariable(name = "token") String token, @PathVariable(name = "videoId") String videoId) {
        if (sp.videoService.streamTokenIsNotValid(token)) {
            throw PitagoraException.forbidden("Unauthorized");
        }

        final Streaming streaming = sp.videoService.loadVideo(videoId);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "video/mp4");
        headers.add("Content-Length", Long.toString(streaming.getLength()));

        return ResponseEntity.ok().headers(headers).body(streaming.getBody());
    }
}
