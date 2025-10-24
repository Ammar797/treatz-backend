package com.treatz.restaurantservice.service;

import com.treatz.restaurantservice.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantService {
    // Restaurant CRUD
    RestaurantResponseDTO createRestaurant(CreateRestaurantRequestDTO request);
    RestaurantResponseDTO getRestaurantById(Long restaurantId); // Returns detailed info with menu items
    List<RestaurantSummaryDTO> getAllRestaurants(); // Returns summary without menu items
    Page<RestaurantSummaryDTO> getAllRestaurants(Pageable pageable); // Paginated summary
    RestaurantResponseDTO updateRestaurant(Long restaurantId, UpdateRestaurantRequestDTO request);
    String deleteRestaurant(Long restaurantId);
    List<MenuItemResponseDTO> getMenuItemsByIds(List<Long> ids);

    // Menu Item CRUD
    MenuItemResponseDTO addMenuItem(Long restaurantId, CreateMenuItemRequestDTO request);
    MenuItemResponseDTO updateMenuItem(Long restaurantId, Long menuItemId, UpdateMenuItemRequestDTO request);
    String deleteMenuItem(Long restaurantId, Long menuItemId);

    // Search Functionality
    List<RestaurantSummaryDTO> searchRestaurantsByName(String name); // Returns summary without menu items
    Page<RestaurantSummaryDTO> searchRestaurantsByName(String name, Pageable pageable); // Paginated summary
    List<MenuItemSearchResponseDTO> searchRestaurantsByMenuItem(String menuItemName);
    Page<MenuItemSearchResponseDTO> searchRestaurantsByMenuItem(String menuItemName, Pageable pageable);
    Long getOwnerIdForRestaurant(Long restaurantId);
}