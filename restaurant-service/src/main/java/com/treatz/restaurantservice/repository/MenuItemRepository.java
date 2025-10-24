package com.treatz.restaurantservice.repository;

import com.treatz.restaurantservice.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
    List<MenuItem> findByNameContainingIgnoreCase(String name);
    Page<MenuItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<MenuItem> findByIdIn(List<Long> ids);
}