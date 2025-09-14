package com.treatz.authservice.dto;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String role;
}