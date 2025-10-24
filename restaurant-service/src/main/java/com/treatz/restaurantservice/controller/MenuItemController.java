package com.treatz.restaurantservice.controller;

import com.treatz.restaurantservice.dto.*;
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

import java.util.List;

@RestController
@RequestMapping("/api") // Note the general base path
@RequiredArgsConstructor
public class MenuItemController {

    private final RestaurantService restaurantService;

    // --- PUBLIC READ AND SEARCH ENDPOINTS ---

    /**
     * Get menu items for a specific restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuForRestaurant(@PathVariable Long restaurantId) {
        RestaurantResponseDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant.getMenuItems());
    }

    /**
     * Search menu items across all restaurants (with optional pagination)
     * Without pagination params: Returns Page with default 100 items
     * With pagination params: Returns specified page
     */
    @GetMapping("/menu-items/search")
    public ResponseEntity<Page<MenuItemSearchResponseDTO>> searchByMenuItem(
            @RequestParam String query,
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
        return ResponseEntity.ok(restaurantService.searchRestaurantsByMenuItem(query, pageable));
    }

    // --- PROTECTED WRITE ENDPOINTS (REQUIRE ROLE_RESTAURANT_OWNER) ---

    @PostMapping("/restaurants/{restaurantId}/menu")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponseDTO> addMenuItem(@PathVariable Long restaurantId, @Valid @RequestBody CreateMenuItemRequestDTO request) {
        MenuItemResponseDTO newItem = restaurantService.addMenuItem(restaurantId, request);
        return new ResponseEntity<>(newItem, HttpStatus.CREATED);
    }

    @PutMapping("/restaurants/{restaurantId}/menu/{menuItemId}")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponseDTO> updateMenuItem(@PathVariable Long restaurantId, @PathVariable Long menuItemId, @Valid @RequestBody UpdateMenuItemRequestDTO request) {
        return ResponseEntity.ok(restaurantService.updateMenuItem(restaurantId, menuItemId, request));
    }

    @DeleteMapping("/restaurants/{restaurantId}/menu/{menuItemId}")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    public ResponseEntity<String> deleteMenuItem(@PathVariable Long restaurantId, @PathVariable Long menuItemId) {
        String message = restaurantService.deleteMenuItem(restaurantId, menuItemId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/menu-items/details")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuItemDetails(@RequestBody List<Long> menuItemIds) {
        // The logic is now simple and correct:
        // 1. We receive a list of IDs.
        // 2. We call the service method that was specifically designed to handle this.
        List<MenuItemResponseDTO> items = restaurantService.getMenuItemsByIds(menuItemIds);
        return ResponseEntity.ok(items);
    }
}