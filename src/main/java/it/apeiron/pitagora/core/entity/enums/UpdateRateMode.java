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
public enum UpdateRateMode {
    UNA_TANTUM(Map.of(IT,"Una Tantum (Effettuata al momento dell'associazione al Dataset)", EN, "One-time (Made at the time of association to the Dataset)", PT, "Acquisição singula (Executado no momento da associação com o Dataset)")),
    PERIODIC(Map.of(IT,"A Intervalli Regolari", EN, "On a regular basis", PT, "Em intervalos regulares"));

    private Map<Language, String> translations;

    public String getDescription(Language language) {
        return translations.get(language);
    }}
