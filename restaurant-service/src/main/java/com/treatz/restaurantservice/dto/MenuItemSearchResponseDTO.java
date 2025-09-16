package com.treatz.restaurantservice.dto;

import lombok.Data;

@Data
public class MenuItemSearchResponseDTO {
    // All the details from MenuItemResponseDTO
    private Long id;
    private String name;
    private String description;
    private Double price;
    private boolean isAvailable;
    private String category;
    private String imageUrl;

    // Plus, the nested restaurant info!
    private RestaurantInfoDTO restaurant;
}