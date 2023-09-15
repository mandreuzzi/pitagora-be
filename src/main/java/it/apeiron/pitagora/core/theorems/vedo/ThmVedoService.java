package it.apeiron.pitagora.core.theorems.vedo;

import it.apeiron.pitagora.core.dto.HttpResponseDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraDataset;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper.FieldMapperRule;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import it.apeiron.pitagora.core.theorems.vedo.entity.FaceDetectionDataset;
import it.apeiron.pitagora.core.theorems.vedo.entity.FaceDetectionModel;
import it.apeiron.pitagora.core.util.Parser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;


@CommonsLog
@RequiredArgsConstructor
@Service
public class ThmVedoService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
        _init();
    }

    @Value("${theorems.vedo.dahua.host}")
    private String dahuaHost;
    @Autowired
    @Qualifier("restTemplateDigest")
    private RestTemplate restTemplateDigest;

    private void _init() {
        Arrays.stream(ThmVedoEntity.values())
                .filter(entity ->
                        sp.datasetService.findByName(entity.pitagoraName()).isEmpty())
                .forEach(entity ->
                        entity.install(sp)
                );

        if (!sp.mongoTemplateData.collectionExists(ThmVedoEntity.FACE_DETECTION.pitagoraName())) {
            find();
        }
    }

    public List<List<String>> find() {
        List<List<String>> dataAsLists = Collections.emptyList();
        try {
            log.info("Calling Dahua Server API...");
            dataAsLists = DahuaFinder.find(restTemplateDigest, dahuaHost);
        } catch (ResourceAccessException e) {
            log.error(e.getMessage());
        }

        PitagoraModel model = sp.modelService.findByName(FaceDetectionModel.FACE_DETECTION_MODEL_NAME)
                .orElseThrow(() -> PitagoraException.notAcceptable("VedoTheorem: Model not found"));

        List<Map<String, Object>> data = _generateDatasetRecords(dataAsLists, model);

        PitagoraDataset ds = sp.datasetService.findByName(FaceDetectionDataset.FACE_DETECTION_DATASET_NAME)
                .orElseThrow(() -> PitagoraException.notAcceptable("VedoTheorem: Dataset not found"));
        sp.dataRecordsService.create(data, ds.getId());

        log.info("Added " + data.size() + " VedoTheorem records");

        return dataAsLists;
    }

    private List<Map<String, Object>> _generateDatasetRecords(List<List<String>> dataAsList, PitagoraModel model) {

        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 0; i < dataAsList.size(); i++) {
            Map<String, Object> record = new HashMap<>();
            dataAsList.get(i).forEach(d -> {
                String key = d.substring(0, d.indexOf("="));
                if (FaceDetectionModel.DAHUA_TO_PITAGORA_KEYS.containsKey(key)) {
                    String stringValue = d.substring(d.indexOf("=") + 1);
                    String modelFieldKey = FaceDetectionModel.DAHUA_TO_PITAGORA_KEYS.get(key);
                    Object typifiedValue = Parser.parse(stringValue, FieldMapperRule.simpleRule(model.getStructure().get(modelFieldKey)),
                            null, null);
                    record.put(modelFieldKey, typifiedValue);
                }
            });
            data.add(record);
        }

        return data;
    }

    public HttpResponseDTO testDahuaServer() {
        log.info("Testing Dahua Server API...");
        HttpResponseDTO res = DahuaFinder.test(restTemplateDigest, dahuaHost);
        if (res.getStatusCode() >= 200 && res.getStatusCode() < 400) {
            log.info("Test return:\n" + res);
        } else {
            log.error("Test return:\n" + res);
        }
        return res;
    }

}
