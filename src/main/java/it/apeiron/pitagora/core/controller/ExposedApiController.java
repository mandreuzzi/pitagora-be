package it.apeiron.pitagora.core.controller;

import it.apeiron.pitagora.core.exception.PitagoraException;
import it.apeiron.pitagora.core.service.ServiceProvider;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CommonsLog
@CrossOrigin
@RequiredArgsConstructor
@RestController
@RequestMapping("public")
public class ExposedApiController {

    private final static String API_KEY_HEADER = "PITAGORA_API_KEY";
    private final static String USER_HEADER = "PITAGORA_USER";

    private final ServiceProvider sp;

    @PostMapping
    public ResponseEntity<String> uploadRecords(@RequestBody List<Map<String, Object>> dataRecords, @RequestHeader(API_KEY_HEADER) String apiKey, @RequestHeader(USER_HEADER) String username) {
        if (apiKey == null || username == null) {
            throw PitagoraException.forbidden("Missing required headers");
        }
        boolean userIsValid = sp.userService.userExistsByEmail(username);
        if (!userIsValid) {
            throw PitagoraException.forbidden("Username not valid");
        }
        sp.dataRecordsService.generateRecordsFromExposedApi(apiKey, dataRecords);
        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }
}
