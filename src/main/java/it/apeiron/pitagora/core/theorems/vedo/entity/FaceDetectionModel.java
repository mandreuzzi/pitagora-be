package it.apeiron.pitagora.core.theorems.vedo.entity;

import it.apeiron.pitagora.core.entity.ModelField;
import it.apeiron.pitagora.core.entity.collection.PitagoraModel;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.Theorem;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Map.entry;

public class FaceDetectionModel extends PitagoraModel {

    public static final String FACE_DETECTION_MODEL_NAME = SYSTEM_RESOURCE_NAME_PREFIX + "Face Detection";

    public static final String FILE_PATH_KEY = "filePath";
    public static final String EVENT_TIME_KEY = "eventTime";
    public static final String SEX_KEY = "sex";
    public static final String AGE_KEY = "age";
    public static final String GLASSES_KEY = "glasses";
    public static final String BEARD_KEY = "beard";

    public static final Map<String, String> DAHUA_TO_PITAGORA_KEYS = Map.ofEntries(
            entry(".SummaryNew[0].Value.ImageInfo.FilePath", FILE_PATH_KEY),
            entry(".EventTime", EVENT_TIME_KEY),
            entry(".SummaryNew[0].Value.Sex", SEX_KEY),
            entry(".SummaryNew[0].Value.Age", AGE_KEY),
            entry(".SummaryNew[0].Value.Glasses", GLASSES_KEY),
            entry(".SummaryNew[0].Value.Beard", BEARD_KEY)
    );

    public static FaceDetectionModel create() {
        return new FaceDetectionModel();
    }

    private FaceDetectionModel() {
        locked = true;
        this.name = FACE_DETECTION_MODEL_NAME;
        structure = new LinkedHashMap<>();
        this.scope.add(Theorem.VEDO);
        this.scope.add(Theorem.ANALYSIS_TOOLS);
        Arrays.asList(
                        ModelField.builder().name(FILE_PATH_KEY).description("File Path").type(FieldType.STRING).build(),
                        ModelField.builder().name(EVENT_TIME_KEY).description("Event Time").type(FieldType.TIMESTAMP).build(),
                        ModelField.builder().name(SEX_KEY).description("Sex").type(FieldType.STRING).build(),
                        ModelField.builder().name(AGE_KEY).description("Age").type(FieldType.INTEGER).build(),
                        ModelField.builder().name(GLASSES_KEY).description("Glasses").type(FieldType.STRING).build(),
                        ModelField.builder().name(BEARD_KEY).description("Beard").type(FieldType.STRING).build()
                )
                .forEach(field -> structure.put(field.getName(), field));
    }

}
