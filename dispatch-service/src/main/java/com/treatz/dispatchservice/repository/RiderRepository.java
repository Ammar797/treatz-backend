package com.treatz.dispatchservice.repository;

import com.treatz.dispatchservice.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider, Long> {
    // A custom Spring Data JPA method to find the first available rider.
    // It's more efficient than fetching all riders and filtering in Java.
    Optional<Rider> findFirstByIsAvailableTrue();
    // In RiderRepository.java
    Optional<Rider> findByUserId(Long userId);
}