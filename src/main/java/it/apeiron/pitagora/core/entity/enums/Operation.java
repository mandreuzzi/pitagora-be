package it.apeiron.pitagora.core.entity.enums;

import static it.apeiron.pitagora.core.util.Language.EN;
import static it.apeiron.pitagora.core.util.Language.IT;
import static it.apeiron.pitagora.core.util.Language.PT;

import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.util.Language;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum Operation {
    EQUALS(Map.of(IT, " == ", EN, " == ", PT, " == ")),
    NOT_EQUALS(Map.of(IT, " != ", EN, " != ", PT, " != ")),
    CONTAINS(Map.of(IT, " contiene ", EN, " contains ", PT, " contém ")),
    NOT_CONTAINS(Map.of(IT, " non contiene ", EN, " not contains ",  PT, " ele não contém ")),
    IS_NULL(Map.of(IT," == null ", EN, " == null ", PT, " == null ")),
    NOT_IS_NULL(Map.of(IT," != null ", EN, " != null ", PT, " != null ")),
    IS_BLANK(Map.of(IT," == stringa vuota ", EN, " == blank string ", PT, " == string vazia ")),
    NOT_IS_BLANK(Map.of(IT," != stringa vuota ", EN, " != blank string ", PT, " != string vazia ")),
    LT(Map.of(IT," < ", EN, " < ", PT, " < ")),
    LTE(Map.of(IT," <= ", EN, " <= ", PT, " <= ")),
    GT(Map.of(IT," > ", EN, " > ", PT, " > ")),
    GTE(Map.of(IT," >= ", EN, " >= ", PT, " >= ")),
    IS_TRUE(Map.of(IT," == true ", EN, " == true ", PT, " == true ")),
    IS_FALSE(Map.of(IT," == false ", EN, " == false ", PT, " == false "));

    private Map<Language, String> translations;

    public String getDescription(Language language) {
        return translations.get(language);
    }

    public static List<Operation> getOperationsByFieldType(FieldType type) {
        switch (type) {
            case STRING:
                return Arrays
                        .asList(EQUALS, NOT_EQUALS, IS_NULL, NOT_IS_NULL, CONTAINS, NOT_CONTAINS, IS_BLANK, NOT_IS_BLANK);
            case INTEGER:
            case DOUBLE:
            case TIMESTAMP:
                return Arrays.asList(EQUALS, NOT_EQUALS, IS_NULL, NOT_IS_NULL, GT, GTE, LT, LTE);
            case BOOLEAN:
                return Arrays.asList(IS_TRUE, IS_FALSE, IS_NULL, NOT_IS_NULL);
            default:
                throw PitagoraException.internalServerError();
        }
    }
}
