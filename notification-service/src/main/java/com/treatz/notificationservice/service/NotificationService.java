package com.treatz.notificationservice.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public void sendOrderPlacedNotification(Long orderId) {
        String message = buildOrderPlacedMessage(orderId);
        sendNotification(message);
    }

    private String buildOrderPlacedMessage(Long orderId) {
        return String.format(
                """
                =======================================
                🔔 NEW ORDER NOTIFICATION
                =======================================
                Order ID: %d
                Status: Order has been placed
                Action Required: Restaurant should accept the order
                =======================================
                """,
                orderId
        );
    }

    private void sendNotification(String message) {
        // For now, just log to console
        // In production: Send email via SendGrid, SMS via Twilio, etc.
        System.out.println(message);
        System.out.println("📧 [Simulated] Email sent to customer");
        System.out.println("📱 [Simulated] SMS sent to restaurant owner\n");
    }
}