package it.apeiron.pitagora.core.service;

import it.apeiron.pitagora.core.dto.ApplicationConstantsDTO;
import it.apeiron.pitagora.core.dto.ValueDescriptionDTO;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser.Role;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplicationService {

    //    private final DocumentsScopeCoordinatesRepo coordinatesRepo;

    @Value("${build.version}")
    private String version;

    public List<ValueDescriptionDTO> getRoles() {
        return Arrays.stream(Role.values())
                .map(value -> new ValueDescriptionDTO(value.name(), value.getDescription())).collect(
                        Collectors.toList());
    }

    public String getVersion() {
        return version;
    }

    public ApplicationConstantsDTO getConstants() {
        return ApplicationConstantsDTO.build(version);
    }

//    public PitagoraScopeCoordinates getCoordinates(String scope) {
//        return coordinatesRepo.findByScope(PitagoraDocumentScope.valueOf(scope))
//                .orElse(PitagoraScopeCoordinates.empty());
//    }
}
