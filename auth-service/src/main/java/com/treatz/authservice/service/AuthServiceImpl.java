package com.treatz.authservice.service;

import com.treatz.authservice.dto.AuthResponseDTO;
import com.treatz.authservice.dto.LoginRequestDTO;
import com.treatz.authservice.dto.RegisterRequestDTO;
import com.treatz.authservice.entity.User;
import com.treatz.authservice.exception.UserAlreadyExistsException;
import com.treatz.authservice.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String register(RegisterRequestDTO registerRequest) {
        // Check if user already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + registerRequest.getEmail() + " already exists");
        }

        // Validate role
        String role = registerRequest.getRole().toUpperCase();
        if (!role.equals("CUSTOMER") && !role.equals("RESTAURANT_OWNER") && !role.equals("RIDER")) {
            throw new IllegalArgumentException("Invalid role. Must be CUSTOMER, RESTAURANT_OWNER, or RIDER");
        }

        // Create new user
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Save user
        userRepository.save(user);

        return "User registered successfully with role: " + role;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getId());

        return new AuthResponseDTO(token, "Login successful! Welcome " + user.getEmail());
    }
}