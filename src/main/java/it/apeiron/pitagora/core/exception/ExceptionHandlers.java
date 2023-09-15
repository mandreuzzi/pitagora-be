package it.apeiron.pitagora.core.exception;

import it.apeiron.pitagora.core.dto.ResponseDTO;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@CommonsLog
@RestControllerAdvice
public class ExceptionHandlers {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseDTO.builder().data(errors).build();
    }

    @ExceptionHandler(PitagoraException.class)
    public ResponseDTO handleEmailAlreadyTakenExceptions(PitagoraException ex, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        return ResponseDTO.builder().message(ex.getMessage()).build();
    }

}
