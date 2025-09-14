package com.treatz.authservice.service;

import com.treatz.authservice.dto.AuthResponseDTO;
import com.treatz.authservice.dto.LoginRequestDTO;
import com.treatz.authservice.dto.RegisterRequestDTO;
import com.treatz.authservice.entity.User;
import com.treatz.authservice.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // Tells Spring that this class is a service component
public class AuthServiceImpl implements AuthService {

    // Give the manager the tools they need
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Constructor to receive the tools (Dependency Injection)
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String register(RegisterRequestDTO registerRequest) {
        // Create a new User ID card from the registration form
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setRole(registerRequest.getRole());
        // Use the secret code machine to hash the password! NEVER store plain text.
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Save the new ID card in the filing cabinet
        userRepository.save(user);

        return "User registered successfully!";
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        // 1. Find the user in the filing cabinet
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // 2. Check if the provided password matches the one from the secret code machine
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. If everything is correct, print a new ID card (JWT)
        String token = jwtService.generateToken(user.getEmail(), user.getRole());

        return new AuthResponseDTO(token, "Login successful!");
    }
}