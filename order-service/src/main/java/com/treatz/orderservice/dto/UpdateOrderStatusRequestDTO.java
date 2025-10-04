package com.treatz.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusRequestDTO {
    @NotBlank(message = "Status cannot be blank")
    private String status;
    // e.g., "ACCEPTED", "PREPARING", etc.
    private Long riderId;
}