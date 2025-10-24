package com.treatz.restaurantservice.controller;

import com.treatz.restaurantservice.dto.CreateRestaurantRequestDTO;
import com.treatz.restaurantservice.dto.RestaurantResponseDTO;
import com.treatz.restaurantservice.dto.RestaurantSummaryDTO;
import com.treatz.restaurantservice.dto.UpdateRestaurantRequestDTO;
import com.treatz.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // --- PUBLIC READ AND SEARCH ENDPOINTS ---

    /**
     * Get all restaurants (with optional pagination)
     * Without pagination params: Returns Page with default 100 items
     * With pagination params: Returns specified page
     * Returns summary info only (no menu items)
     */
    @GetMapping
    public ResponseEntity<Page<RestaurantSummaryDTO>> getAllRestaurants(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 100,
                Sort.by(direction, sortBy)
        );
        return ResponseEntity.ok(restaurantService.getAllRestaurants(pageable));
    }

    /**
     * Search restaurants by name (with optional pagination)
     * Returns summary info only (no menu items)
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RestaurantSummaryDTO>> searchRestaurants(
            @RequestParam String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 100,
                Sort.by(direction, sortBy)
        );
        return ResponseEntity.ok(restaurantService.searchRestaurantsByName(name, pageable));
    }

    /**
     * Get single restaurant by ID
     * Returns full details including menu items
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    /**
     * Internal endpoint: Get owner ID for a restaurant
     */
    @GetMapping("/{id}/owner")
    public ResponseEntity<Long> getOwnerId(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getOwnerIdForRestaurant(id));
    }

    // --- PROTECTED WRITE ENDPOINTS (REQUIRE ROLE_RESTAURANT_OWNER) ---

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(@Valid @RequestBody CreateRestaurantRequestDTO request) {
        RestaurantResponseDTO createdRestaurant = restaurantService.createRestaurant(request);
        return new ResponseEntity<>(createdRestaurant, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(@PathVariable Long id, @Valid @RequestBody UpdateRestaurantRequestDTO request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long id) {
        String message = restaurantService.deleteRestaurant(id);
        return ResponseEntity.ok(message);
    }
}