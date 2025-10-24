package com.treatz.restaurantservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RestaurantSummaryDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private BigDecimal rating;
}