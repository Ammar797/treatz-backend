package com.treatz.dispatchservice.listener;

import com.treatz.dispatchservice.config.RabbitMQConfig;
import com.treatz.dispatchservice.dto.OrderDTO;
import com.treatz.dispatchservice.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// In dispatch-service's OrderListener.java

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderListener {

    private final DispatchService dispatchService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderEvents(OrderDTO orderDTO) {
        log.info("📩 [RabbitMQ] Received order event for Order ID: {} with status: {}",
                 orderDTO.getId(), orderDTO.getStatus());

        try {
            if ("READY_FOR_PICKUP".equals(orderDTO.getStatus())) {
                log.info("🚨 Order {} is READY_FOR_PICKUP - assigning rider...", orderDTO.getId());
                dispatchService.processOrderForDispatch(orderDTO);
            } else if ("DELIVERED".equals(orderDTO.getStatus())) {
                log.info("📦 Order {} is DELIVERED - releasing rider...", orderDTO.getId());
                dispatchService.releaseRiderForOrder(orderDTO);
            } else {
                log.debug("ℹ️ Ignoring event with status '{}' for Order {}", orderDTO.getStatus(), orderDTO.getId());
            }
        } catch (RuntimeException e) {
            // Better error handling with specific message
            log.error("❌ Failed to process event for Order {}: {}", orderDTO.getId(), e.getMessage());
            // Message stays in queue and can be retried if needed
        } catch (Exception e) {
            // Unexpected error
            log.error("💥 Unexpected error processing Order {}: {}", orderDTO.getId(), e.getMessage(), e);
        }
    }
}