package com.treatz.dispatchservice.dto;

import lombok.Data;
import java.math.BigDecimal;

// This is the "contract" for the message we receive from the Order Service.
// It's a simplified version of the Order entity.
@Data
public class OrderDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private BigDecimal totalPrice;
    private String status;
    private Long riderId;
}