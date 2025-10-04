// In DispatchScheduler.java
package com.treatz.dispatchservice.scheduler;

import com.treatz.dispatchservice.dto.OrderDTO;
import com.treatz.dispatchservice.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchScheduler {

    private final WebClient.Builder webClientBuilder;
    private final DispatchService dispatchService;

    // This method will run automatically every 60 seconds.
    @Scheduled(fixedRate = 60000)
    public void findAndProcessStuckOrders() {
        log.info("Scheduler running: Looking for stuck READY_FOR_PICKUP orders...");

        try {
            List<OrderDTO> stuckOrders = webClientBuilder.build()
                    .get()
                    .uri("http://order-service/api/orders/internal/status/READY_FOR_PICKUP")
                    .retrieve()
                    .bodyToFlux(OrderDTO.class)
                    .collectList()
                    .block();

            if (stuckOrders != null && !stuckOrders.isEmpty()) {
                log.info("Found {} stuck orders. Attempting to dispatch...", stuckOrders.size());
                for (OrderDTO order : stuckOrders) {
                    // For each stuck order, we try to run our existing dispatch logic.
                    try {
                        dispatchService.processOrderForDispatch(order);
                    } catch (Exception e) {
                        log.warn("Scheduler failed to dispatch stuck order {}: {}", order.getId(), e.getMessage());
                        // This is okay, it means no riders were free. We will try again on the next run.
                    }
                }
            } else {
                log.info("No stuck orders found.");
            }
        } catch (Exception e) {
            log.error("Scheduler failed to fetch stuck orders from Order Service.", e);
        }
    }
}