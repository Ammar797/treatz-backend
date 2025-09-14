package com.treatz.authservice.service;

import com.treatz.authservice.dto.AuthResponseDTO;
import com.treatz.authservice.dto.LoginRequestDTO;
import com.treatz.authservice.dto.RegisterRequestDTO;

public interface AuthService {
    String register(RegisterRequestDTO registerRequest);
    AuthResponseDTO login(LoginRequestDTO loginRequest);
}