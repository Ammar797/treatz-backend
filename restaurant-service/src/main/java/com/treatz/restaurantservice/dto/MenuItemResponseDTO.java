package com.treatz.restaurantservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean available;
    private String category; // Keeping as String for JSON compatibility
    private String imageUrl;
}