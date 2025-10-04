package com.treatz.orderservice.repository;

import com.treatz.orderservice.entity.Order;
import com.treatz.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // This method finds all orders for a given restaurant ID AND a given status
    List<Order> findByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
    List<Order> findAllByStatus(OrderStatus status);
}