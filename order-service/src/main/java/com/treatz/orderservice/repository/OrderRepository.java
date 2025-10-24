package com.treatz.orderservice.repository;

import com.treatz.orderservice.entity.Order;
import com.treatz.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Find orders for a restaurant with specific status
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);

    // Find all orders by status (for internal services)
    List<Order> findAllByStatus(OrderStatus status);

    // Find all orders for a customer (order history)
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}