package com.treatz.orderservice.service;

import com.treatz.orderservice.dto.CreateOrderRequestDTO;
import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.dto.UpdateOrderStatusRequestDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequest);
    List<OrderResponseDTO> getOrdersForRestaurant(Long restaurantId, String status);
    OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request);
    List<OrderResponseDTO> findAllByStatus(String status);
}