package com.treatz.orderservice.entity;

public enum PaymentStatus {
    PENDING,      // Payment not yet completed
    COMPLETED,    // Payment successful
    FAILED,       // Payment failed
    REFUNDED      // Money returned (if order cancelled)
}