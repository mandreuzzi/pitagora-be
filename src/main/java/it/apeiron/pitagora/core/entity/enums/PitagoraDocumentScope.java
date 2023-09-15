package it.apeiron.pitagora.core.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PitagoraDocumentScope {
    CORE("", true),
    DAHUA("Dahua", true);

    private String description;
    private boolean forClient;
}
