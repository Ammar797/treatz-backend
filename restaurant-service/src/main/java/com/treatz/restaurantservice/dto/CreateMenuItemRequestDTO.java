package com.treatz.restaurantservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CreateMenuItemRequestDTO {
    @NotBlank(message = "Item name cannot be blank")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be a positive number")
    private Double price;

    @NotBlank(message = "Category cannot be blank")
    private String category; // e.g., STARTER, MAIN_COURSE

    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;
}