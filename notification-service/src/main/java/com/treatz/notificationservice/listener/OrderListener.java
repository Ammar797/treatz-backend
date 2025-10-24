package com.treatz.notificationservice.listener;

import com.treatz.notificationservice.config.RabbitMQConfig;
import com.treatz.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderPlaced(Long orderId) {
        System.out.println("\n📩 [RabbitMQ] Message received from queue: " + RabbitMQConfig.QUEUE_NAME);
        notificationService.sendOrderPlacedNotification(orderId);
    }
}