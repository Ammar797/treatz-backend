package com.treatz.orderservice.mapper;

import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.entity.Order;
import com.treatz.orderservice.entity.OrderStatus;
import com.treatz.orderservice.entity.PaymentMethod;
import com.treatz.orderservice.entity.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // Maps Order entity to OrderResponseDTO
    @Mapping(source = "items", target = "items")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "paymentStatus", expression = "java(order.getPaymentStatus().name())")
    @Mapping(target = "paymentMethod", expression = "java(order.getPaymentMethod().name())")
    OrderResponseDTO orderToResponseDTO(Order order);

    // Helper methods for enum to string conversion
    default String mapOrderStatus(OrderStatus status) {
        return status != null ? status.name() : null;
    }

    default String mapPaymentStatus(PaymentStatus status) {
        return status != null ? status.name() : null;
    }

    default String mapPaymentMethod(PaymentMethod method) {
        return method != null ? method.name() : null;
    }
}