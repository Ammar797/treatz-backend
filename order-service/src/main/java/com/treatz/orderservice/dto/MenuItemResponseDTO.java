package com.treatz.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price; // Updated to BigDecimal for proper money handling
    private boolean available;
    private String category;
    private String imageUrl;
}