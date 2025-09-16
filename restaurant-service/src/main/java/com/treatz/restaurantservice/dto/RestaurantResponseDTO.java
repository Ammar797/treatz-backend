package com.treatz.restaurantservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class RestaurantResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private Double rating;
    private List<MenuItemResponseDTO> menuItems;
}