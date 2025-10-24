package com.treatz.orderservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDTO {
    private Long id;
    private Long customerId;
    private Long restaurantId;
    private Long riderId;
    private BigDecimal totalPrice;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentTransactionId;
    private String deliveryAddress;
    private String customerPhone;
    private String deliveryInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponseDTO> items;
}
