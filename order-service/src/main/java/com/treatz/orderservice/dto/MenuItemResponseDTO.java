package com.treatz.orderservice.dto;

import lombok.Data;

@Data
public class MenuItemResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private boolean available;
    private String category;
    private String imageUrl;
}