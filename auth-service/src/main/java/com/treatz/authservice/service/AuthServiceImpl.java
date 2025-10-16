package com.treatz.authservice.service;

import com.treatz.authservice.dto.AuthResponseDTO;
import com.treatz.authservice.dto.LoginRequestDTO;
import com.treatz.authservice.dto.RegisterRequestDTO;
import com.treatz.authservice.entity.User;
import com.treatz.authservice.exception.UserAlreadyExistsException;
import com.treatz.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public String register(RegisterRequestDTO registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User with email " + registerRequest.getEmail() + " already exists"
            );
        }

        String role = validateAndNormalizeRole(registerRequest.getRole());

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        userRepository.save(user);

        return "User registered successfully with role: " + role;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());

        return new AuthResponseDTO(token, "Login successful! Welcome " + user.getEmail());
    }

    private String validateAndNormalizeRole(String role) {
        String normalizedRole = role.toUpperCase();

        if (!normalizedRole.equals("CUSTOMER") &&
                !normalizedRole.equals("RESTAURANT_OWNER") &&
                !normalizedRole.equals("RIDER")) {
            throw new IllegalArgumentException(
                    "Invalid role. Must be CUSTOMER, RESTAURANT_OWNER, or RIDER"
            );
        }

        return normalizedRole;
    }
}