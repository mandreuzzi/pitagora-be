package it.apeiron.pitagora.core.controller;

import it.apeiron.pitagora.core.dto.ResponseDTO;
import it.apeiron.pitagora.core.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@CommonsLog
@RequiredArgsConstructor
@RestController
@RequestMapping
public class ApplicationController {

	private final ApplicationService service;

	@GetMapping("/roles")
	public ResponseEntity<ResponseDTO> getRoles() {
		return ResponseDTO.ok(service.getRoles());
	}

	@GetMapping("/status")
	public ResponseEntity<Void> health() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("/version")
	public ResponseEntity<ResponseDTO> getVersion() {
		return ResponseDTO.ok(service.getVersion());
	}

	@GetMapping("/constants")
	public ResponseEntity<ResponseDTO> getConstants() {
		return ResponseDTO.ok(service.getConstants());
	}

}
