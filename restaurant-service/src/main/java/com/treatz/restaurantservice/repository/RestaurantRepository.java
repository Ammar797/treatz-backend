package com.treatz.restaurantservice.repository;

import com.treatz.restaurantservice.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
}