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
        // This listener is now a central hub for all order-related events.
        try {
            if ("READY_FOR_PICKUP".equals(orderDTO.getStatus())) {
                log.info("Received a READY_FOR_PICKUP event for Order ID {}", orderDTO.getId());
                dispatchService.processOrderForDispatch(orderDTO);
            } else if ("DELIVERED".equals(orderDTO.getStatus())) {
                log.info("Received a DELIVERED event for Order ID {}", orderDTO.getId());
                dispatchService.releaseRiderForOrder(orderDTO); // We will build this method next
            }
        } catch (Exception e) {
            log.error("Error processing event for Order ID {}: {}", orderDTO.getId(), e.getMessage());
        }
    }
}