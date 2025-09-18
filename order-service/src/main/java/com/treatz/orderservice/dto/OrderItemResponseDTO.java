package com.treatz.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private Long menuItemId;
    private Integer quantity;
    private BigDecimal pricePerItem;
}