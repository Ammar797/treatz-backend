package com.treatz.orderservice.dto;

import com.treatz.orderservice.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequestDTO {
    @NotNull(message = "Restaurant ID cannot be null")
    private Long restaurantId;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDTO> items;

    // Payment Information
    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    // Delivery Information
    @NotBlank(message = "Delivery address cannot be blank")
    @Size(min = 10, max = 500, message = "Delivery address must be between 10 and 500 characters")
    private String deliveryAddress;

    @NotBlank(message = "Customer phone cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid (10-15 digits)")
    private String customerPhone;

    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;  // Optional
}