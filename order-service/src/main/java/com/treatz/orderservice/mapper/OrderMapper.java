package com.treatz.orderservice.mapper;

import com.treatz.orderservice.dto.OrderResponseDTO;
import com.treatz.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // This tells MapStruct to map the "items" field from the entity to the "items" field in the DTO.
    @Mapping(source = "items", target = "items")
    OrderResponseDTO orderToResponseDTO(Order order);
}