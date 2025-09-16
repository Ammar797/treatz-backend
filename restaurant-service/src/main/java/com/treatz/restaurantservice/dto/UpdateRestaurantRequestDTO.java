package com.treatz.restaurantservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateRestaurantRequestDTO {

    @NotBlank(message = "Restaurant name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phone;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid contact email")
    private String email;

    @NotNull(message = "Active status cannot be null")
    private Boolean active;
}