package it.apeiron.pitagora.core.service;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.JSON_ROOT_TYPE_DETECTION_FAILED;
import static it.apeiron.pitagora.core.util.Parser.parse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.apeiron.pitagora.core.entity.HttpMapperExtraConfig;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.exception.PitagoraException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;

@CommonsLog
@Service
public class JsonService {

    private ServiceProvider sp;
    public void setSp(ServiceProvider sp) {
        this.sp = sp;
    }

    private final ObjectMapper om = new ObjectMapper();

    public void checkTypeMatching(PitagoraMapper mapper, String jsonString) {
        _generateDatasetRecords(mapper, jsonString, true);
    }
    public List<Map<String, Object>> generateDatasetRecords(PitagoraMapper mapper, String jsonString) {
        return _generateDatasetRecords(mapper, jsonString, false);
    }

    private List<Map<String, Object>> _generateDatasetRecords(PitagoraMapper mapper, String jsonString, boolean justACheck) {
        List<Map<String, Object>> mappedDataset = new ArrayList<>();
        HttpMapperExtraConfig extraConfig = om.convertValue(mapper.getExtraConfig(), HttpMapperExtraConfig.class);
        JsonElement rootElement = getJsonElement(JsonParser.parseString(jsonString), extraConfig.getRootJsonPath());

        if (rootElement.isJsonObject() && !extraConfig.isRootIsArray()) {
            mappedDataset.add(populateDatasetRow(rootElement, mapper, extraConfig.getRootJsonPath(), false));
        } else if (rootElement.isJsonArray() && extraConfig.isRootIsArray()) {
            if (justACheck) {
                populateDatasetRow(rootElement.getAsJsonArray().get(0), mapper, extraConfig.getRootJsonPath(), true);
            }
            rootElement.getAsJsonArray().forEach(element -> mappedDataset.add(populateDatasetRow(element, mapper, extraConfig.getRootJsonPath(), true)));
        } else {
            log.error("RootJson object/array detection failed");
            throw PitagoraException.notAcceptable(t(JSON_ROOT_TYPE_DETECTION_FAILED));
        }

        return mappedDataset;
    }

    private Map<String, Object> populateDatasetRow(JsonElement parent, PitagoraMapper mapper,
            String rootJsonPath, boolean rootJsonIsArray) {
        Map<String, Object> datasetRow = new HashMap<>();
        mapper.getRules().forEach((fieldName, fieldAndRuleDefinition) -> {
            String currentJsonElementName = fieldAndRuleDefinition.getRuleImpl();
            JsonElement currentJsonElement = getJsonElement(parent,
                    currentJsonElementName);

            Object typifiedValue = parse(currentJsonElement, fieldAndRuleDefinition, SourceChannel.HTTP, "ROOT." + rootJsonPath + (rootJsonIsArray ? "[...]" : "") + "." + currentJsonElementName);

            datasetRow.put(fieldName, typifiedValue);

        });
        return datasetRow;
    }

    private JsonElement getJsonElement(JsonElement json, String path){

        String[] parts = path.split("[.\\[\\]]");
        JsonElement result = json;

        for (String key : parts) {

            key = key.trim();
            if (key.isEmpty())
                continue;

            if (result == null){
                result = JsonNull.INSTANCE;
                break;
            }

            if (result.isJsonObject()){
                result = ((JsonObject)result).get(key);
            }
            else if (result.isJsonArray()){
                int ix = Integer.parseInt(key);
                try {
                    result = ((JsonArray) result).get(ix);
                } catch (IndexOutOfBoundsException e) {
                    result = null;
                    break;
                }
            }
            else break;
        }

        return result;
    }
}
