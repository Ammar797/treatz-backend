package com.treatz.orderservice.service;

import com.treatz.orderservice.dto.CreateOrderRequestDTO;
import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.dto.UpdateOrderStatusRequestDTO;

import java.util.List;

public interface OrderService {
    // Create new order
    OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequest);

    // Restaurant owner views orders
    List<OrderResponseDTO> getOrdersForRestaurant(Long restaurantId, String status);

    // Update order status
    OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request);

    // Internal service - find by status
    List<OrderResponseDTO> findAllByStatus(String status);

    // Customer views their order history
    List<OrderResponseDTO> getMyOrders();

    // Customer views specific order details
    OrderResponseDTO getOrderById(Long orderId);
}