package it.apeiron.pitagora.core.util;

import it.apeiron.pitagora.core.service.auth.JwtService;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum Language {
    IT("Italiano"),
    EN("English"),
    PT("Português");

    private String description;
    public static JwtService JWT_SERVICE;

    public static String t(MessagesCore... msgs) {
        return JWT_SERVICE.getLoggedUserLanguage()._t(msgs);
    }

    public static String tANT(MessagesAnalysisTools... msgs) {
        return JWT_SERVICE.getLoggedUserLanguage()._tANT(msgs);
    }

    public String _t(MessagesCore... msgs) {
        return Arrays.stream(msgs).map(msg -> msg.translateInto(this)).collect(Collectors.joining(" "));
    }

    private String _tANT(MessagesAnalysisTools... msgs) {
        return Arrays.stream(msgs).map(msg -> msg.translateInto(this)).collect(Collectors.joining(" "));
    }
}
