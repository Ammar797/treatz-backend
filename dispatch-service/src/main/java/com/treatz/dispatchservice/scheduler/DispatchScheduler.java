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

    // This method runs automatically every 60 seconds to catch missed dispatch events
    @Scheduled(fixedRate = 60000)
    public void findAndProcessStuckOrders() {
        log.info("⏰ [Scheduler] Checking for stuck READY_FOR_PICKUP orders...");

        try {
            List<OrderDTO> stuckOrders = webClientBuilder.build()
                    .get()
                    .uri("http://order-service/api/orders/internal/status/READY_FOR_PICKUP")
                    .retrieve()
                    .bodyToFlux(OrderDTO.class)
                    .collectList()
                    .block();

            if (stuckOrders != null && !stuckOrders.isEmpty()) {
                log.warn("⚠️ Found {} stuck orders without riders. Attempting to dispatch...", stuckOrders.size());
                int successCount = 0;
                int failCount = 0;

                for (OrderDTO order : stuckOrders) {
                    try {
                        dispatchService.processOrderForDispatch(order);
                        successCount++;
                    } catch (RuntimeException e) {
                        failCount++;
                        log.warn("❌ Could not dispatch order {}: {}", order.getId(), e.getMessage());
                        // This is expected if no riders are available. Will retry in 60 seconds.
                    }
                }

                log.info("✅ Scheduler results: {} dispatched, {} failed (will retry)", successCount, failCount);
            } else {
                log.debug("✓ No stuck orders found. All orders properly dispatched.");
            }
        } catch (Exception e) {
            log.error("💥 Scheduler error while fetching orders from Order Service: {}", e.getMessage(), e);
        }
    }
}