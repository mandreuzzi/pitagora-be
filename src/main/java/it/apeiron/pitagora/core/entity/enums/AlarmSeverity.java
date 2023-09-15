package it.apeiron.pitagora.core.entity.enums;

import static it.apeiron.pitagora.core.util.Language.EN;
import static it.apeiron.pitagora.core.util.Language.IT;
import static it.apeiron.pitagora.core.util.Language.PT;

import it.apeiron.pitagora.core.util.Language;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum AlarmSeverity {

    HIGH(Map.of(IT,"Alta", EN, "High", PT, "Alto")),
    MEDIUM(Map.of(IT,"Media", EN, "Medium", PT, "Média")),
    LOW(Map.of(IT,"Bassa", EN, "Low", PT, "Baixo"));

    private Map<Language, String> translations;

    public String getDescription(Language language) {
        return translations.get(language);
    }


}
