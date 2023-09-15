package it.apeiron.pitagora.core.exception;

import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.EXC_DETAIL_CSV;
import static it.apeiron.pitagora.core.util.MessagesCore.EXC_DETAIL_EXCEL;
import static it.apeiron.pitagora.core.util.MessagesCore.EXC_DETAIL_JSON;
import static it.apeiron.pitagora.core.util.MessagesCore.NUMBER_FORMAT_EX_TITLE;
import static it.apeiron.pitagora.core.util.MessagesCore.TIMESTAMP_EX_FIELD_DETAIL;
import static it.apeiron.pitagora.core.util.MessagesCore.TIMESTAMP_EX_TITLE;

import it.apeiron.pitagora.core.entity.enums.FieldType;
import it.apeiron.pitagora.core.entity.enums.SourceChannel;
import it.apeiron.pitagora.core.util.MessagesCore;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class PitagoraException extends RuntimeException {

    private HttpStatus status;
    private String message;

    public static PitagoraException internalServerError() {
        return PitagoraException.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    public static PitagoraException notAcceptable(String message) {
        return PitagoraException.builder()
                .status(HttpStatus.NOT_ACCEPTABLE)
                .message(message)
                .build();
    }

    public static PitagoraException forbidden(String message) {
        return PitagoraException.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(message)
                .build();
    }

    public static PitagoraException badRequest(String message) {
        return PitagoraException.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();
    }

    public static PitagoraException numberFormatEx(String fieldDescription, FieldType type,
            String detail, SourceChannel channel, String extra) {
        String message = t(NUMBER_FORMAT_EX_TITLE)
                .replace("FIELD_DESCRIPTION", fieldDescription)
                .replace("FIELD_TYPE_DESCRIPTION", type.name());
        message += _buildElementDetail(channel, detail);
        if (StringUtils.isNotEmpty(extra)) {
            message += "  (" + extra + ")";
        }
        return PitagoraException.builder()
                .status(HttpStatus.PRECONDITION_FAILED)
                .message(message)
                .build();
    }

    private static String _buildElementDetail(SourceChannel channel, String detail) {
        switch (channel) {
            case HTTP:
                return t(EXC_DETAIL_JSON).replace("DETAIL", detail);
            case FILE_EXCEL:
                return t(EXC_DETAIL_EXCEL).replace("DETAIL", detail);
            case FILE_CSV:
                return t(EXC_DETAIL_CSV).replace("DETAIL", detail);
            default:
                return "";
        }
    }

    public static PitagoraException timestampPattern(String fieldDescription, FieldType type,
            String detail, SourceChannel channel) {
        String message = t(TIMESTAMP_EX_TITLE);
        if (channel != null) {
            message += _buildElementDetail(channel, detail);
        }
        message += t(TIMESTAMP_EX_FIELD_DETAIL)
                .replace("FIELD_DESCRIPTION", fieldDescription)
                .replace("FIELD_TYPE_DESCRIPTION", type.name());

        return PitagoraException.builder()
                .status(HttpStatus.PRECONDITION_FAILED)
                .message(message)
                .build();
    }

    public static PitagoraException nameNotAvailable() {
        return PitagoraException.notAcceptable(t(MessagesCore.NAME_NOT_AVAILABLE));
    }
}
