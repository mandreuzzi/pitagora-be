package it.apeiron.pitagora.core.dto.auth;

import it.apeiron.pitagora.core.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	private String token;
	private UserDTO userInfo;
}
