package it.apeiron.pitagora.core.entity.collection;

import it.apeiron.pitagora.core.dto.UserDTO;
import it.apeiron.pitagora.core.entity.enums.Theorem;
import it.apeiron.pitagora.core.util.Language;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("user")
public class PitagoraUser {

    @Id
    private String id;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String company;
    private String area;
    private String phone;
    private String other;
    private Role role;
    private Language language;
    private List<Theorem> theorems;
    private Map<Theorem, Object> preferences;

    public static PitagoraUser fromDto(UserDTO d) {
        return PitagoraUser.builder()
                .id(d.getId())
                .language(Language.valueOf(d.getLanguage().toUpperCase()))
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .company(d.getCompany())
                .area(d.getArea())
                .phone(d.getPhone())
                .email(d.getEmail().trim())
                .other(d.getOther())
                .role(Role.valueOf(d.getRole().getValue()))
                .theorems(d.getTheorems() != null ? d.getTheorems() : Collections.emptyList())
                .build();
    }

    public void update(UserDTO d) {
        firstName = d.getFirstName();
        language = Language.valueOf(d.getLanguage().toUpperCase());
        lastName = d.getLastName();
        company = d.getCompany();
        area = d.getArea();
        phone = d.getPhone();
        other = d.getOther();
        role = Role.valueOf(d.getRole().getValue());
        theorems = d.getTheorems() != null ? d.getTheorems() : Collections.emptyList();
    }

    public static class Privileges {
        public final static String CAN_READ_DOCUMENTS = "CAN_READ_DOCUMENTS";
        public final static String CAN_WRITE_DOCUMENTS = "CAN_WRITE_DOCUMENTS";
    }

    @AllArgsConstructor
    public enum Role {

//        SUPER_ADMIN(new String[]{CAN_WRITE_DOCUMENTS}),
//        ADMIN(new String[]{CAN_WRITE_DOCUMENTS}),
//        ANALYST(new String[]{CAN_READ_DOCUMENTS}),
//        USER(new String[]{CAN_READ_DOCUMENTS});
//        private final String[] authorities;

        SUPER_ADMIN("Super Amministratore"),
        ADMIN("Amministratore"),
        ANALYST("Analista"),
        USER("Utente base");

        private String description;

        public String[] getAuthorities() {
            return new String[]{this.name()};
        }
        public String getDescription() {
            return description;
        }
    }

}
