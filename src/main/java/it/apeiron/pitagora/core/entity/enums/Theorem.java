package it.apeiron.pitagora.core.entity.enums;


import it.apeiron.pitagora.core.theorems.dire.DireUserPreferences;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum Theorem {
    ANALYSIS_TOOLS("Analysis Tools", new HashMap<>()),
    LOGICO("Logico", new HashMap<>()),
    VEDO("Vedo", new HashMap<>()),
    LUX("Lux", new HashMap<>()),
    DIRE("Dire", DireUserPreferences.buildDefault()),
    INFRALOGIC("Infralogic", new HashMap<>()),
    LEPSYS("Lepsys", new HashMap<>());

    private String description;
    private Object userPreferences;
}
