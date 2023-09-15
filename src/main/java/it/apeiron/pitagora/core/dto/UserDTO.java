package it.apeiron.pitagora.core.dto;

import it.apeiron.pitagora.core.dto.auth.LoginRequest;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class UserDTO extends LoginRequest {

    // TODO aggiungere validazioni sui campi
    private String id;
    private String language;
    private String firstName;
    private String lastName;
    private String company;
    private String area;
    private String phone;
    private String other;
    private ValueDescriptionDTO role;
    private List<Theorem> theorems;

    public static UserDTO fromModel(PitagoraUser m) {
        return UserDTO.builder()
                .id(m.getId())
                .language(m.getLanguage().name().toLowerCase())
                .email(m.getEmail())
                .firstName(m.getFirstName())
                .lastName(m.getLastName())
                .company(m.getCompany())
                .area(m.getArea())
                .phone(m.getPhone())
                .other(m.getOther())
                .role(new ValueDescriptionDTO(m.getRole().name(), m.getRole().getDescription()))
                .theorems(m.getTheorems())
                .build();
    }
}
