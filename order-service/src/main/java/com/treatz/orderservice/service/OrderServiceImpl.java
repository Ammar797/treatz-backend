package com.treatz.orderservice.service;

import com.treatz.orderservice.config.RabbitMQConfig;
import com.treatz.orderservice.dto.CreateOrderRequestDTO;
import com.treatz.orderservice.dto.MenuItemResponseDTO; // This might need to be created or adjusted
import com.treatz.orderservice.dto.OrderItemResponseDTO;
import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.entity.Order;
import com.treatz.orderservice.entity.OrderItem;
import com.treatz.orderservice.entity.OrderStatus;
import com.treatz.orderservice.exception.InvalidOrderStatusTransitionException;
import com.treatz.orderservice.exception.ResourceNotFoundException;
import com.treatz.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient; // For making API calls
import com.treatz.orderservice.mapper.OrderMapper;
import com.treatz.orderservice.dto.UpdateOrderStatusRequestDTO;
import org.springframework.security.access.AccessDeniedException;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder; // Tool for calling other services
    private final RabbitTemplate rabbitTemplate; // Our "Postal Worker" for sending messages
    private final OrderMapper orderMapper;

    @Override
    public OrderResponseDTO createOrder(CreateOrderRequestDTO createOrderRequest) {
        // === Step 1: Get User Info ===
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long customerId = principal.getClaim("userId");

        // === Step 2: Call the Restaurant Service to get menu item details ===
        // We collect all the IDs of the menu items the customer wants to order.
        List<Long> menuItemIds = createOrderRequest.getItems().stream()
                .map(item -> item.getMenuItemId())
                .toList();

        // We use our WebClient "telephone" to call the Restaurant Service.
        // This is a SYNCHRONOUS call. The Order Service will wait for a response.
        List<MenuItemResponseDTO> menuItemDetails = webClientBuilder.build()
                .post()
                .uri("http://restaurant-service/api/menu-items/details") // Eureka helps us find the service by name!
                .bodyValue(menuItemIds)
                .retrieve()
                .bodyToFlux(MenuItemResponseDTO.class)
                .collectList()
                .block(); // .block() makes it wait for the response

        // === Step 3: Validate the Order ===
        if (menuItemDetails == null || menuItemDetails.size() != menuItemIds.size()) {
            throw new IllegalArgumentException("One or more menu items could not be found.");
        }

        // Convert the list to a map for easy lookup
        Map<Long, MenuItemResponseDTO> menuItemMap = menuItemDetails.stream()
                .collect(Collectors.toMap(MenuItemResponseDTO::getId, item -> item));

        // === Step 4: Build the Order Entity ===
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(createOrderRequest.getRestaurantId());
        order.setStatus(OrderStatus.PENDING);

        // Set payment information
        order.setPaymentMethod(createOrderRequest.getPaymentMethod());
        order.setPaymentStatus(com.treatz.orderservice.entity.PaymentStatus.PENDING);

        // Set delivery information
        order.setDeliveryAddress(createOrderRequest.getDeliveryAddress());
        order.setCustomerPhone(createOrderRequest.getCustomerPhone());
        order.setDeliveryInstructions(createOrderRequest.getDeliveryInstructions());

        List<OrderItem> orderItems = createOrderRequest.getItems().stream().map(reqItem -> {
            MenuItemResponseDTO details = menuItemMap.get(reqItem.getMenuItemId());
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(reqItem.getMenuItemId());
            orderItem.setQuantity(reqItem.getQuantity());
            orderItem.setPricePerItem(details.getPrice()); // Already BigDecimal, no valueOf needed
            orderItem.setOrder(order);
            return orderItem;
        }).toList();

        order.setItems(orderItems);

        // Calculate total price
        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        // === Step 5: Process Payment ===
        processPayment(order);

        // === Step 6: Save the Order to the Database ===
        Order savedOrder = orderRepository.save(order);

        // === Step 6: PUBLISH THE EVENT! ===
        // The mail is ready to be sent. We give it to our postal worker (RabbitTemplate).
        System.out.println("Publishing order placed event for order ID: " + savedOrder.getId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ORDER_PLACED_ROUTING_KEY,
                savedOrder.getId() // We'll just send the order ID for now
        );

        // === Step 7: Return the Response ===
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomerId());
        response.setRestaurantId(savedOrder.getRestaurantId());
        response.setRiderId(savedOrder.getRiderId());
        response.setTotalPrice(savedOrder.getTotalPrice());
        response.setStatus(savedOrder.getStatus().toString());
        response.setPaymentStatus(savedOrder.getPaymentStatus().toString());
        response.setPaymentMethod(savedOrder.getPaymentMethod().toString());
        response.setPaymentTransactionId(savedOrder.getPaymentTransactionId());
        response.setDeliveryAddress(savedOrder.getDeliveryAddress());
        response.setCustomerPhone(savedOrder.getCustomerPhone());
        response.setDeliveryInstructions(savedOrder.getDeliveryInstructions());
        response.setCreatedAt(savedOrder.getCreatedAt());
        response.setUpdatedAt(savedOrder.getUpdatedAt());

        List<OrderItemResponseDTO> itemDTOs = savedOrder.getItems().stream().map(item -> {
            OrderItemResponseDTO dto = new OrderItemResponseDTO();
            dto.setMenuItemId(item.getMenuItemId());
            dto.setQuantity(item.getQuantity());
            dto.setPricePerItem(item.getPricePerItem());
            return dto;
        }).toList();
        response.setItems(itemDTOs);

        return response;
    }

    // In OrderServiceImpl.java

    @Override
    public List<OrderResponseDTO> getOrdersForRestaurant(Long restaurantId, String status) {
        // === Step 1: Get the ID of the user making the request from the JWT ===
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long authenticatedUserId = principal.getClaim("userId");

        // === Step 2: Call the Restaurant Service to find out who the real owner is ===
        Long actualOwnerId = webClientBuilder.build()
                .get()
                .uri("http://restaurant-service/api/restaurants/" + restaurantId + "/owner")
                .retrieve()
                .bodyToMono(Long.class)
                .block();

        // === Step 3: THE CRITICAL SECURITY CHECK ===
        if (!actualOwnerId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("User is not authorized to view these orders.");
        }

        // === Step 4: If security passes, fetch the orders from the database ===
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        List<Order> orders = orderRepository.findByRestaurantIdAndStatus(restaurantId, orderStatus);

        // === Step 5: Map the results to DTOs and return them ===
        return orders.stream()
                .map(orderMapper::orderToResponseDTO)
                .collect(Collectors.toList());
    }

    // In OrderServiceImpl.java

    // In OrderServiceImpl.java

    // In OrderServiceImpl.java

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request) {
        // === Step 1: Find the order and get the new status ===
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        OrderStatus currentStatus = order.getStatus();

        // === Step 2: THE CRITICAL FIX - Check the type of caller BEFORE accessing the JWT ===
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isExternalUser = authentication != null && authentication.getPrincipal() instanceof Jwt;

        if (isExternalUser) {
            // --- This is a REAL USER with a JWT (Restaurant Owner or Rider) ---
            Jwt principal = (Jwt) authentication.getPrincipal();
            Long authenticatedUserId = principal.getClaim("userId");

            // We check the user's official granted authorities.
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RESTAURANT_OWNER"))) {
                // --- Security & State Logic for RESTAURANT_OWNER ---
                Long actualOwnerId = webClientBuilder.build().get().uri("http://restaurant-service/api/restaurants/" + order.getRestaurantId() + "/owner").retrieve().bodyToMono(Long.class).block();
                if (!actualOwnerId.equals(authenticatedUserId)) {
                    throw new AccessDeniedException("User is not authorized to update this order.");
                }

                switch (newStatus) {
                    case ACCEPTED:
                        if (currentStatus != OrderStatus.PENDING) throw new InvalidOrderStatusTransitionException("Can only accept a PENDING order.");
                        break;
                    case PREPARING:
                        if (currentStatus != OrderStatus.ACCEPTED) throw new InvalidOrderStatusTransitionException("Can only prepare an ACCEPTED order.");
                        break;
                    case READY_FOR_PICKUP:
                        if (currentStatus != OrderStatus.PREPARING) throw new InvalidOrderStatusTransitionException("Can only mark a PREPARING order as ready.");
                        break;
                    default:
                        throw new InvalidOrderStatusTransitionException("Restaurant owner cannot change status to " + newStatus);
                }
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RIDER"))) {
                // --- Security & State Logic for RIDER ---
                switch (newStatus) {
                    case DELIVERED:
                        if (currentStatus != OrderStatus.DISPATCHED) throw new InvalidOrderStatusTransitionException("Can only deliver a DISPATCHED order.");
                        if (order.getRiderId() == null || !order.getRiderId().equals(authenticatedUserId)) {
                            throw new AccessDeniedException("Rider is not authorized to deliver this order.");
                        }
                        break;
                    default:
                        throw new InvalidOrderStatusTransitionException("Rider cannot change status to " + newStatus);
                }
            } else {
                throw new AccessDeniedException("User is not authorized to update order status.");
            }
        } else {
            // --- This is an INTERNAL, anonymous call (from Dispatch Service) ---
            // We trust this call and only check the state transition.
            if (newStatus != OrderStatus.DISPATCHED || currentStatus != OrderStatus.READY_FOR_PICKUP) {
                throw new InvalidOrderStatusTransitionException("Internal service can only change status from READY_FOR_PICKUP to DISPATCHED.");
            }
        }

        // === Step 3: If all rules pass, update the order object ===
        order.setStatus(newStatus);
        // Set the rider ID if it was provided in the request (from Dispatch Service or Rider)
        if (request.getRiderId() != null) {
            order.setRiderId(request.getRiderId());
        } else if (isExternalUser && "ROLE_RIDER".equals(((Jwt) authentication.getPrincipal()).getClaim("role")) && newStatus == OrderStatus.DISPATCHED){
            // If a rider is dispatching, assign them to the order
            order.setRiderId(((Jwt) authentication.getPrincipal()).getClaim("userId"));
        }

        // === Step 4: Save to database, publish event, and return response ===
        Order updatedOrder = orderRepository.save(order);
        String routingKey = "order.status." + newStatus.name().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, updatedOrder);
        System.out.println("Published event with routing key: " + routingKey);

        return orderMapper.orderToResponseDTO(updatedOrder);
    }
    @Override
    public List<OrderResponseDTO> findAllByStatus(String status) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findAllByStatus(orderStatus).stream()
                .map(orderMapper::orderToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDTO> getMyOrders() {
        // Get customer ID from JWT
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long customerId = principal.getClaim("userId");

        // Fetch all orders for this customer, newest first
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        // Map to DTOs
        return orders.stream()
                .map(orderMapper::orderToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        // Find the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Security check: Only the customer who placed the order can view it
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long authenticatedUserId = principal.getClaim("userId");

        if (!order.getCustomerId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }

        // Return the order details
        return orderMapper.orderToResponseDTO(order);
    }

    // === PAYMENT PROCESSING (Simulated) ===
    private void processPayment(Order order) {
        com.treatz.orderservice.entity.PaymentMethod method = order.getPaymentMethod();

        if (method == com.treatz.orderservice.entity.PaymentMethod.CASH_ON_DELIVERY) {
            // Cash on delivery - no payment verification needed
            order.setPaymentStatus(com.treatz.orderservice.entity.PaymentStatus.COMPLETED);
            order.setPaymentTransactionId("COD-" + System.currentTimeMillis());
            System.out.println("💵 Payment: Cash on Delivery - No verification needed");
        } else {
            // Card/UPI payment - simulate payment gateway call
            // In real world, you'd call Stripe/Razorpay API here
            boolean paymentSuccess = simulatePaymentGateway(order);

            if (paymentSuccess) {
                order.setPaymentStatus(com.treatz.orderservice.entity.PaymentStatus.COMPLETED);
                order.setPaymentTransactionId(method.name() + "-" + System.currentTimeMillis());
                System.out.println("✅ Payment successful via " + method + " - Transaction ID: " + order.getPaymentTransactionId());
            } else {
                order.setPaymentStatus(com.treatz.orderservice.entity.PaymentStatus.FAILED);
                throw new IllegalArgumentException("Payment failed. Please check your payment details and try again.");
            }
        }
    }

    // Simulates payment gateway response
    private boolean simulatePaymentGateway(Order order) {
        // In real world: Call Stripe/Razorpay API, handle webhooks
        // For demo: Always return true (payment successful)
        System.out.println("🔄 Simulating payment gateway call for amount: $" + order.getTotalPrice());
        return true;  // Payment always succeeds in simulation
    }
}