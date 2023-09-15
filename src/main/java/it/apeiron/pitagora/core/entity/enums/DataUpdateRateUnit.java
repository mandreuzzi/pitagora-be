package it.apeiron.pitagora.core.entity.enums;

import static it.apeiron.pitagora.core.util.Language.EN;
import static it.apeiron.pitagora.core.util.Language.IT;
import static it.apeiron.pitagora.core.util.Language.PT;

import it.apeiron.pitagora.core.util.Language;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DataUpdateRateUnit {
    DAYS (Map.of(IT, "giorni", EN, "giorni", PT, "dias")),
    HOURS(Map.of(IT, "ore", EN, "ore", PT, "horas")),
    MINUTES(Map.of(IT, "minuti", EN, "minuti", PT, "minutos"));
//    SECONDS(Map.of(IT, "secondi", EN, "seconds"));

    private Map<Language, String> translations;

    public String getDescription(Language language) {
        return translations.get(language);
    }
}
