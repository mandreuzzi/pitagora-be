package it.apeiron.pitagora.core.dto;

import static it.apeiron.pitagora.core.service.DataRecordsService.FAKE_MODEL_ATTRIBUTE_FOR_ID;
import static it.apeiron.pitagora.core.util.Language.JWT_SERVICE;

import it.apeiron.pitagora.core.entity.enums.AlarmSeverity;
import it.apeiron.pitagora.core.entity.enums.DataUpdateRateUnit;
import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.NotificationChannel;
import it.apeiron.pitagora.core.entity.enums.Operation;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.entity.enums.UpdateRateMode;
import it.apeiron.pitagora.core.util.Language;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class ApplicationConstantsDTO {

    private String version;
    private Map<String, ValueDescriptionDTO> theoremsCode = new HashMap<>();
    private final List<ValueDescriptionDTO> fieldTypeOptions = new ArrayList<>();
    private final Map<String, List<ValueDescriptionDTO>> allFilterOperations = new HashMap<>();
    private final List<ValueDescriptionDTO> httpSourceUpdateOptions = new ArrayList<>();
    private final List<ValueDescriptionDTO> httpSourceUpdateRateOptions = new ArrayList<>();
    private final List<ValueDescriptionDTO> notificationChannelOptions = new ArrayList<>();
    private final List<ValueDescriptionDTO> severityOptions = new ArrayList<>();
    private final List<ValueDescriptionDTO> languageOptions = new ArrayList<>();

    private String fakeFieldType = "";

    public static ApplicationConstantsDTO build(String version) {
        Language language = JWT_SERVICE.getLoggedUserLanguage();
        ApplicationConstantsDTO c = new ApplicationConstantsDTO();
        c.version = version;
        c.languageOptions.addAll(Arrays.stream(Language.values())
                .map(op -> new ValueDescriptionDTO(op.name().toLowerCase(), op.getDescription())).collect(
                        Collectors.toList()));
        Arrays.stream(Theorem.values()).forEach(thm -> {
            c.theoremsCode.put(thm.name(), new ValueDescriptionDTO(thm.name(), thm.getDescription()));
        });

        c.fieldTypeOptions.addAll(Arrays.stream(FieldType.values())
                .map(op -> new ValueDescriptionDTO(op.name())).collect(
                        Collectors.toList()));
        Arrays.stream(FieldType.values()).forEach(type -> {
            List<ValueDescriptionDTO> ops = Operation.getOperationsByFieldType(type).stream()
                    .map(op -> new ValueDescriptionDTO(op.name(), op.getDescription(language))).collect(
                            Collectors.toList());
            c.allFilterOperations.put(type.name(), ops);
        });
        c.httpSourceUpdateOptions.addAll(Arrays.stream(UpdateRateMode.values())
                .map(op -> new ValueDescriptionDTO(op.name(), op.getDescription(language))).collect(
                        Collectors.toList()));
        c.httpSourceUpdateRateOptions.addAll(Arrays.stream(DataUpdateRateUnit.values())
                .map(op -> new ValueDescriptionDTO(op.name(), op.getDescription(language))).collect(
                        Collectors.toList()));
        c.notificationChannelOptions.addAll(Arrays.stream(NotificationChannel.values())
                .map(op -> new ValueDescriptionDTO(op.name(), op.getDescription())).collect(
                        Collectors.toList()));
        c.severityOptions.addAll(Arrays.stream(AlarmSeverity.values())
                .map(op -> new ValueDescriptionDTO(op.name(), op.getDescription(language))).collect(
                        Collectors.toList()));

        c.fakeFieldType = FAKE_MODEL_ATTRIBUTE_FOR_ID;

        return c;
    }
}
