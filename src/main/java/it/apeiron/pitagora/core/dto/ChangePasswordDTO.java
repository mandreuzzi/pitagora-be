package it.apeiron.pitagora.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordDTO {

    private String oldPassword;
    private String password;
    private String confirmPassword;
}
