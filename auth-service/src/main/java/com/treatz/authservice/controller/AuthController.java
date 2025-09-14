package com.treatz.authservice.controller;

import com.treatz.authservice.dto.AuthResponseDTO;
import com.treatz.authservice.dto.LoginRequestDTO;
import com.treatz.authservice.dto.RegisterRequestDTO;
import com.treatz.authservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        // Now call the real service
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(null, message));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
        // Now call the real service
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}