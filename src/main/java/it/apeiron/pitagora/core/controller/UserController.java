package it.apeiron.pitagora.core.controller;

import static it.apeiron.pitagora.core.util.Language.JWT_SERVICE;
import static it.apeiron.pitagora.core.util.Language.t;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CHANGED_F;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_CREATED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_DELETED;
import static it.apeiron.pitagora.core.util.MessagesCore.SUCCESSFULLY_UPDATED;
import static it.apeiron.pitagora.core.util.MessagesCore.USER;

import it.apeiron.pitagora.core.dto.ChangePasswordDTO;
import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.dto.UserDTO;
import it.apeiron.pitagora.core.dto.auth.AuthResponse;
import it.apeiron.pitagora.core.dto.auth.LoginRequest;
import it.apeiron.pitagora.core.entity.collection.PitagoraUser;
import it.apeiron.pitagora.core.service.auth.JwtService;
import it.apeiron.pitagora.core.service.auth.UserService;
import it.apeiron.pitagora.core.util.Language;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CommonsLog
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("auth")
public class UserController {

	private final JwtService jwtService;

	private final UserService userService;


	//	@PreAuthorize("hasAuthority('RRO')")
	@PostMapping
	public ResponseEntity<ResponseDTO> create(@Valid @RequestBody UserDTO user) {
		userService.create(user);
		return ResponseDTO.created(t(USER, SUCCESSFULLY_CREATED));
	}

	@PutMapping
	public ResponseEntity<ResponseDTO> update(@RequestBody UserDTO user) {
		userService.update(user);
		String msg;
		if (JWT_SERVICE.getLoggedUser().getId().equals(user.getId())) {
			msg = Language.valueOf(user.getLanguage().toUpperCase())._t(USER, SUCCESSFULLY_UPDATED);
		} else {
			msg = t(USER, SUCCESSFULLY_UPDATED);
		}
		return ResponseDTO.ok(null, msg);
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
		PitagoraUser user = userService
				.loadUserByUsernameAndValidatePassword(loginRequest.getEmail(), loginRequest.getPassword());
		return ResponseDTO.ok(new AuthResponse(jwtService.generateToken(user), UserDTO.fromModel(user)));
	}

	@GetMapping("/validate")
	public ResponseEntity<ResponseDTO> validate() {
		return ResponseDTO.ok("JWT is valid");
	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseDTO> logout(HttpServletRequest request) {
		jwtService.invalidateToken(request);
		return ResponseDTO.ok("Logout successful");
	}

	@PostMapping("/checkEmail")
	public ResponseEntity<ResponseDTO> checkEmailExistence(@RequestBody String username) {
		return ResponseDTO.ok(userService.checkEmailExistence(username));
	}

	@GetMapping("/user")
	public ResponseEntity<ResponseDTO> findAll(@RequestParam("q") String query) {
		return ResponseDTO.ok(userService.findAll(query));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<ResponseDTO> findById(@PathVariable("userId") ObjectId userId) {
		return ResponseDTO.ok(userService.findById(userId));
	}

	@DeleteMapping("/user/{userId}")
	public ResponseEntity<ResponseDTO> delete(@PathVariable("userId") ObjectId userId) {
		userService.delete(userId);
		return ResponseDTO.ok(null, t(USER, SUCCESSFULLY_DELETED));
	}

	@PostMapping("/changePassword")
	public ResponseEntity<ResponseDTO> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
		userService.changePassword(dto);
		return ResponseDTO.ok(null, "Password " + t(USER, SUCCESSFULLY_CHANGED_F));
	}
}
