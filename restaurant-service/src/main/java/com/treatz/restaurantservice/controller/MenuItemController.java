package com.treatz.restaurantservice.controller;

import com.treatz.restaurantservice.dto.*;
import com.treatz.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuForRestaurant(@PathVariable Long restaurantId) {
        // We can get this from the getRestaurantById method in the service if we enhance it,
        // but for now this is fine as the service method already checks if the restaurant exists.
        RestaurantResponseDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant.getMenuItems());
    }

    @GetMapping("/menu-items/search")
    public ResponseEntity<List<MenuItemSearchResponseDTO>> searchByMenuItem(@RequestParam String query) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByMenuItem(query));
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
}