package com.treatz.orderservice.service;

import com.treatz.orderservice.dto.CreateOrderRequestDTO;
import com.treatz.orderservice.dto.OrderResponseDTO;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequest);
}