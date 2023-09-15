package it.apeiron.pitagora.core.entity.collection;


import it.apeiron.pitagora.core.dto.MapperDTO;
import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@CommonsLog
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("mapper")
public class PitagoraMapper extends AbstractPitagoraRecord {

    protected ObjectId referenceSourceId;
    protected ObjectId referenceModelId;
    protected SourceChannel channel;
    protected Map<String, FieldMapperRule> rules;
    protected Object extraConfig;

    public PitagoraMapper(MapperDTO dto, SourceChannel channel) {
        update(dto, channel);
    }

    public void update(MapperDTO d, SourceChannel ch) {
        /** DELETE THIS TO RE-ENABLE SELECTION OF TIMESTAMP PATTERN (also frontend must be restored)
         */
//        Optional<FieldMapperRule> wrongTimestampPattern = d.getRules().values().stream()
//                .filter(rule ->
//                        ModelFieldType.TIMESTAMP.equals(rule.getType())
//                                && !Parser.PATTERNS.contains(rule.getTimestampPattern()))
//                .findFirst();
//        if (wrongTimestampPattern.isPresent()) {
//            log.error("Timestamp pattern unknown: " + wrongTimestampPattern.get().timestampPattern);
//            throw PitagoraException.notAcceptable("Il pattern \"" + wrongTimestampPattern.get().timestampPattern + "\" non è ammesso");
//        }
/** */

        superUpdate(d);
        channel = ch;
        referenceSourceId = new ObjectId(d.getReferenceSourceId());
        referenceModelId = new ObjectId(d.getReferenceModelId());
        rules = d.getRules();

        /** DELETE THIS TO RE-ENABLE SELECTION OF TIMESTAMP PATTERN (also frontend must be restored)
         */
        rules = new HashMap<>();
        d.getRules().keySet().forEach(k -> {
            FieldMapperRule v = d.getRules().get(k);
            if (FieldType.TIMESTAMP.equals(v.getType())) {
                v.setTimestampPattern("AUTO");
            }
            rules.put(k, v);
        });
        /** */

        extraConfig = d.getExtraConfig();
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    public static class FieldMapperRule extends ModelField {
        private String ruleImpl;
        private String timestampPattern;

        public static FieldMapperRule simpleRule(ModelField modelField) {
            return FieldMapperRule.builder()
                    .name(modelField.getName())
                    .description(modelField.getDescription())
                    .type(modelField.getType())
                    .build();
        }
    }
}
