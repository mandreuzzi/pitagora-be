package it.apeiron.pitagora.core.theorems.lux.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraMapper;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.theorems.lux.ThmLuxEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TelemetryMapper extends PitagoraMapper {

    public static TelemetryMapper create(PitagoraModel model) {
        return new TelemetryMapper(model);
    }

    private TelemetryMapper(PitagoraModel model) {
        locked = true;
        name = ThmLuxEntity.TELEMETRY.pitagoraName();
        scope.add(Theorem.ANALYSIS_TOOLS);
        scope.add(Theorem.LUX);
        referenceModelId = model.getId();
        channel = SourceChannel.HTTP;
        extraConfig = Map.of("rootJsonPath", "_items", "rootIsArray", true);
        rules = new LinkedHashMap<>();

        ObjectMapper om = new ObjectMapper();
        model.getStructure().forEach((fieldName, field) -> {
            FieldMapperRule rule = om.convertValue(field, FieldMapperRule.class);
            rule.setRuleImpl(fieldName);
            rules.put(fieldName, rule);
        });
    }

}
