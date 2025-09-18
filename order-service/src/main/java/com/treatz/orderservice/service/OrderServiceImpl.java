package com.treatz.orderservice.service;

import com.treatz.orderservice.config.RabbitMQConfig;
import com.treatz.orderservice.dto.CreateOrderRequestDTO;
import com.treatz.orderservice.dto.MenuItemResponseDTO; // This might need to be created or adjusted
import com.treatz.orderservice.dto.OrderItemResponseDTO;
import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.entity.Order;
import com.treatz.orderservice.entity.OrderItem;
import com.treatz.orderservice.entity.OrderStatus;
import com.treatz.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient; // For making API calls

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@EnableJpaAuditing
@EnableDiscoveryClient

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder; // Tool for calling other services
    private final RabbitTemplate rabbitTemplate; // Our "Postal Worker" for sending messages

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

        List<OrderItem> orderItems = createOrderRequest.getItems().stream().map(reqItem -> {
            MenuItemResponseDTO details = menuItemMap.get(reqItem.getMenuItemId());
            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(reqItem.getMenuItemId());
            orderItem.setQuantity(reqItem.getQuantity());
            orderItem.setPricePerItem(BigDecimal.valueOf(details.getPrice()));
            orderItem.setOrder(order);
            return orderItem;
        }).toList();

        order.setItems(orderItems);

        // Calculate total price
        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getPricePerItem().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        // === Step 5: Save the Order to the Database ===
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
        // We need a mapper for this, but for now, let's do it manually
        OrderResponseDTO response = new OrderResponseDTO();
        response.setId(savedOrder.getId());
        response.setCustomerId(savedOrder.getCustomerId());
        response.setRestaurantId(savedOrder.getRestaurantId());
        response.setTotalPrice(savedOrder.getTotalPrice());
        response.setStatus(savedOrder.getStatus().toString());
        response.setCreatedAt(savedOrder.getCreatedAt());

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
}