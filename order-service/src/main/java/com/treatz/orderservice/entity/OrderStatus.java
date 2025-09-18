package com.treatz.orderservice.entity;

public enum OrderStatus {
    PENDING,
    ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    DISPATCHED,
    DELIVERED,
    CANCELLED
}