package com.treatz.restaurantservice.dto;

import com.treatz.restaurantservice.entity.MenuCategory;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
public class CreateMenuItemRequestDTO {
    @NotBlank(message = "Item name cannot be blank")
    @Size(min = 2, max = 255, message = "Item name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "Description cannot be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @DecimalMax(value = "99999.99", message = "Price cannot exceed 99999.99")
    @Digits(integer = 5, fraction = 2, message = "Price must have at most 5 digits and 2 decimal places")
    private BigDecimal price;

    @NotNull(message = "Category cannot be null")
    private MenuCategory category;

    @URL(message = "Image URL must be a valid URL")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;

    @NotNull(message = "Availability status cannot be null")
    private Boolean available;
}