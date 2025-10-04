package com.treatz.dispatchservice.service;

import com.treatz.dispatchservice.dto.OrderDTO;
import com.treatz.dispatchservice.entity.Rider;
import com.treatz.dispatchservice.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispatchServiceImpl implements DispatchService {

    private final RiderRepository riderRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public void processOrderForDispatch(OrderDTO orderDTO) {
        log.info("Processing order {} for dispatch.", orderDTO.getId());

        // Step 1: Find an available rider from our database.
        Rider availableRider = riderRepository.findFirstByIsAvailableTrue()
                .orElseThrow(() -> new RuntimeException("No available riders found for order " + orderDTO.getId()));

        log.info("Found available rider: {} (User ID: {})", availableRider.getName(), availableRider.getUserId());

        // Step 2: Assign the order by making the rider unavailable.
        availableRider.setAvailable(false);
        riderRepository.save(availableRider);
        log.info("Rider {} is now assigned and unavailable.", availableRider.getName());

        // Step 3: Notify the Order Service that the order has been dispatched.
        try {
            // THE CRITICAL CHANGE: Create a request body that includes BOTH the status and the rider's ID.
            Map<String, Object> requestBody = Map.of(
                    "status", "DISPATCHED",
                    "riderId", availableRider.getUserId()
            );

            webClientBuilder.build()
                    .put()
                    .uri("http://order-service/api/orders/" + orderDTO.getId() + "/status")
                    .bodyValue(requestBody) // Send the new, richer body
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Successfully updated order {} status to DISPATCHED and assigned rider {}.", orderDTO.getId(), availableRider.getName());

        } catch (Exception e) {
            log.error("Failed to update order status for order {}. Reverting rider availability.", orderDTO.getId(), e);
            // This is a crucial rollback step!
            availableRider.setAvailable(true);
            riderRepository.save(availableRider);
            // In a real system, we would re-queue this message for another attempt.
        }
    }

    @Override
    public void releaseRiderForOrder(OrderDTO orderDTO) {
        if (orderDTO.getRiderId() == null) {
            log.warn("Cannot release rider for order {}: riderId is null in the event payload.", orderDTO.getId());
            return;
        }

        Rider rider = riderRepository.findByUserId(orderDTO.getRiderId())
                .orElse(null);

        if (rider == null) {
            log.warn("Could not find rider profile for userId {} to release.", orderDTO.getRiderId());
            return;
        }

        rider.setAvailable(true);
        riderRepository.save(rider);
        log.info("Rider {} (User ID: {}) has completed delivery for order {} and is now available.", rider.getName(), rider.getUserId(), orderDTO.getId());
    }
}