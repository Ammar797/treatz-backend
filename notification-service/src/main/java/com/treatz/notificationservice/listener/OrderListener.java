package com.treatz.notificationservice.listener;

import com.treatz.notificationservice.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    // This magic annotation tells Spring: "Connect this method to the 'notification_queue' mailbox.
    // Run this code whenever a new message arrives."
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderPlaced(Long orderId) {
        // For now, our "notification" is just a log message to the console.
        System.out.println("🔔 NOTIFICATION RECEIVED! 🔔");
        System.out.println("A new order has been placed. Order ID: " + orderId);
        System.out.println("Testing ....Sending a simulated email/SMS to the customer...");
    }
}