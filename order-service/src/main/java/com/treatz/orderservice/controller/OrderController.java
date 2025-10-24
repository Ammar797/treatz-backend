package com.treatz.orderservice.controller;

import com.treatz.orderservice.dto.CreateOrderRequestDTO;
import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.treatz.orderservice.dto.UpdateOrderStatusRequestDTO;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    // This is the security rule: Only users with the 'CUSTOMER' authority can access this.
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody CreateOrderRequestDTO request) {
        OrderResponseDTO createdOrder = orderService.createOrder(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // In OrderController.java

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersForRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam String status) { // e.g., ?status=PENDING

        List<OrderResponseDTO> orders = orderService.getOrdersForRestaurant(restaurantId, status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
// This security rule allows EITHER a RESTAURANT_OWNER OR a RIDER to call this endpoint.
   // @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER') or hasAuthority('ROLE_RIDER')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO request) {

        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }
    @GetMapping("/internal/status/{status}")
// This is a secure endpoint, but we don't need role checks as we assume the caller is trusted.
// In a real system, we'd secure this with a specific internal-service role or mTLS.
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByStatus(@PathVariable String status) {
        // We will build this service method next.
        List<OrderResponseDTO> orders = orderService.findAllByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // === CUSTOMER ORDER APIs ===

    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        List<OrderResponseDTO> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

}