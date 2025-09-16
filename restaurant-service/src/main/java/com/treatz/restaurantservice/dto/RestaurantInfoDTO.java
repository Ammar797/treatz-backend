package com.treatz.restaurantservice.dto;

import lombok.Data;

@Data
public class RestaurantInfoDTO {
    private Long id;
    private String name;
    private String address;
    private Double rating;
}