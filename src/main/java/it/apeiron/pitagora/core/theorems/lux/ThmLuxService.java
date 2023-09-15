package it.apeiron.pitagora.core.theorems.lux;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraData;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.lux.entity.DeviceEnrichedTelemetryModel;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmLuxService {

    private ServiceProvider sp;

    public void setSp(ServiceProvider sp) {
        this.sp = sp;
        _init();
    }

    @Value("${theorems.lux.api.url}")
    private String apiurl;
    @Value("${theorems.lux.api.username}")
    private String username;
    @Value("${theorems.lux.api.password}")
    private String password;
    @Value("${theorems.lux.api.clientId}")
    private String clientId;

    private final RestTemplate restTemplate;
    private final ObjectMapper om = new ObjectMapper();

    private String accessToken;

    private void _init() {
        Arrays.stream(ThmLuxEntity.values())
                .filter(entity ->
                        sp.datasetService.findByName(entity.pitagoraName()).isEmpty())
                .forEach(entity ->
                        entity.install(sp)
                );

        List.of(ThmLuxEntity.DEVICE, ThmLuxEntity.TELEMETRY).forEach(this::_fetchRecords);
        _enrichDeviceWithTelemetry();
    }

    private void _enrichDeviceWithTelemetry() {
        if (sp.mongoTemplateData.collectionExists(ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY.pitagoraName())) {
            return;
        }
        log.info("LUX - Enriching Devices with Telemetry ...");
        LookupOperation lookup = Aggregation
                .lookup(ThmLuxEntity.TELEMETRY.pitagoraName(), "data." + "_id", "data._fk_device",
                        "aggregations");
        List<PitagoraData> mappedResults = sp.mongoTemplateData
                .aggregate(Aggregation.newAggregation(lookup), ThmLuxEntity.DEVICE.pitagoraName(), PitagoraData.class)
                .getMappedResults();

        DeviceEnrichedTelemetryModel m = DeviceEnrichedTelemetryModel.create();
        List<Map<String, Object>> records = sp.dataRecordsService.extractEnrichedRecordsFromLookupResults(mappedResults, m.getAggregatedModelDTO());

        PitagoraDataset ds = sp.datasetService.findByName(ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY.pitagoraName()).get();
        sp.dataRecordsService.create(records, ds.getId());
        log.info("Enrichment done: " + records.size() + " records created on Dataset [" + ThmLuxEntity.DEVICE_ENRICHED_TELEMETRY.pitagoraName() + "]");
    }

    public void login() {
        String authUrl = apiurl + "/login";
        RequestEntity<Map> req = RequestEntity.post(authUrl)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
                .body(Map.of("login", username, "password", password));
        ResponseEntity<Map> res = restTemplate.exchange(authUrl, HttpMethod.POST, req, Map.class);

        accessToken = (String) res.getBody().get("access_token");
    }

    public HttpResponseDTO makeRequest(String endpoint, HttpMethod method, Object body) {
        return _makeRequest(endpoint, method, body, 0);
    }

    public HttpResponseDTO makeRequest(String endpoint, HttpMethod method) {
        return _makeRequest(endpoint, method, null, 0);
    }

    private HttpResponseDTO _makeRequest(String endpoint, HttpMethod method, Object body, int retry) {
        String url = apiurl + "/" + endpoint;
        if (StringUtils.isEmpty(accessToken)) {
            login();
        }

        RequestEntity req = RequestEntity
                .method(method, url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(body);

        try {

            ResponseEntity<String> resp = restTemplate.exchange(url, method, req, String.class);
            return HttpResponseDTO.builder()
                    .statusCode(resp.getStatusCodeValue())
                    .responseBody(resp.getBody()).build();
        } catch (HttpClientErrorException e) {

            if (HttpStatus.UNAUTHORIZED.equals(e.getStatusCode())) {
                if (retry < 5) {
                    login();
                    retry++;
                    return _makeRequest(endpoint, method, body, retry);
                }
                log.error("Authentication error on ModulusOne API server");
                throw PitagoraException.internalServerError();
            }

            return HttpResponseDTO.builder()
                    .statusCode(e.getRawStatusCode())
                    .responseBody(e.getMessage()).build();
        } catch (Exception e) {
            return HttpResponseDTO.builder()
                    .responseBody(e.getMessage()).build();
        }
    }

    @SneakyThrows
    public void _fetchRecords(ThmLuxEntity entity) {

        if (sp.mongoTemplateData.collectionExists(entity.pitagoraName())) {
            return;
        }

        PitagoraDataset dataset = sp.datasetService.findByName(entity.pitagoraName()).get();
        PitagoraMapper mapper = sp.mapperService.findByName(entity.pitagoraName()).get();

        int currentPageNumber = 0;
        boolean nextExists;
        int total = 0;
        log.info("LUX - Loading ModulusOne objects from " + entity.endpoint() + " ...");
        do {
            currentPageNumber++;
            try {
                String url = entity.endpoint() + "?max_results=250&page=" + currentPageNumber;
                String currentPageRes = sp.thmLuxService.makeRequest(url, HttpMethod.GET).getResponseBody();
                Map currentPage = om.readValue(currentPageRes, Map.class);

                List<Map<String, Object>> records = sp.jsonService.generateDatasetRecords(mapper, currentPageRes);

                save(records, dataset.getId());

                nextExists = currentPage.containsKey("_links") && ((Map) currentPage.get("_links")).containsKey("next");

                total += records.size();
            } catch (Exception e) {
                log.error(e);
                nextExists = false;
            }

        } while (nextExists && currentPageNumber < 20);

        log.info("Retrieved " + total + " records");
    }

    @Transactional
    public void save(List<Map<String, Object>> data, ObjectId deviceDatasetId) {
        sp.dataRecordsService.create(data, deviceDatasetId);
    }

    public Map<String, Object> getCredentials() {
        return Map.of("username", username, "password", password);
    }
}
