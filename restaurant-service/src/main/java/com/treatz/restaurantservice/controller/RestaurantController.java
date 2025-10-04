package com.treatz.restaurantservice.controller;

import com.treatz.restaurantservice.dto.CreateRestaurantRequestDTO;
import com.treatz.restaurantservice.dto.RestaurantResponseDTO;
import com.treatz.restaurantservice.dto.UpdateRestaurantRequestDTO;
import com.treatz.restaurantservice.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    // --- PUBLIC READ AND SEARCH ENDPOINTS ---

    @GetMapping
    public ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponseDTO>> searchRestaurants(@RequestParam String name) {
        return ResponseEntity.ok(restaurantService.searchRestaurantsByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    // !!! THIS IS THE NEW, MISSING ENDPOINT !!!
    // This is a special, internal-facing endpoint for other services.
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