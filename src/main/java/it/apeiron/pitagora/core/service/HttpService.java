package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.entity.enums.SourceChannel.HTTP;
import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.SOURCE_MUST_BE_HTTP;

import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.dto.HttpSourceDTO;
import it.apeiron.pitagora.core.entity.HttpSource;
import it.apeiron.pitagora.core.entity.collection.PitagoraHttpResponse;
import it.apeiron.pitagora.core.entity.collection.PitagoraSource;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.repository.PitagoraHttpResponseRepository;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@CommonsLog
@RequiredArgsConstructor
@Service
public class HttpService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final PitagoraHttpResponseRepository httpResponseRepository;
    private final RestTemplate restTemplate;

    public HttpResponseDTO buildPreview(PitagoraSource source) {
        if (!HTTP.equals(source.getChannel())) {
            log.error("SourceChannel must be HTTP");
            throw PitagoraException.notAcceptable(t(SOURCE_MUST_BE_HTTP));
        }
        PitagoraHttpResponse response = httpResponseRepository.findById(source.getHttpConfiguration().getSampleResponseId())
                .orElseThrow(() -> PitagoraException.badRequest("HttpResponse with id " + source.getHttpConfiguration().getSampleResponseId().toString() + " not found"));
        return HttpResponseDTO.fromModel(response);
    }

    public HttpResponseDTO getHttpSourceResponseNow(HttpSourceDTO dto) {
        return getHttpSourceResponseNow(new HttpSource(dto));
    }

    public HttpResponseDTO getHttpSourceResponseNow(HttpSource http) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        http.getHeaders().forEach((k,v) -> headers.put(k, Collections.singletonList(v)));
        String urlWithParam = http.getUrl();
        if (!http.getParams().isEmpty()) {
            List<String> params = new ArrayList<>();
            http.getParams().forEach((k,v) -> params.add(k + "=" + v));
            urlWithParam += "?" + String.join("&", params);
        }
        RequestEntity<Object> req = new RequestEntity<Object>(http.getBody(), headers, http.getMethod(), URI.create(urlWithParam));
        try {
            ResponseEntity<String> res = restTemplate.exchange(req, String.class);
            return HttpResponseDTO.builder()
                    .statusCode(res.getStatusCodeValue())
                    .responseBody(res.getBody()).build();
        } catch (HttpClientErrorException e) {
            return HttpResponseDTO.builder()
                    .statusCode(e.getRawStatusCode())
                    .responseBody(e.getMessage()).build();
        } catch (Exception e) {
            return HttpResponseDTO.builder()
                    .responseBody(e.getMessage()).build();
        }
    }

    @Transactional
    public ObjectId saveResponse(HttpResponseDTO res, ObjectId sourceId) {
        PitagoraHttpResponse saved = httpResponseRepository.save(PitagoraHttpResponse.create(res, sourceId));
        return saved.getId();
    }

}
