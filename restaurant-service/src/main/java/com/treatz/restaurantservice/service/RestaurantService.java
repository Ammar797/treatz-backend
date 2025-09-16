package com.treatz.restaurantservice.service;

import com.treatz.restaurantservice.dto.*;
import java.util.List;

public interface RestaurantService {
    // Restaurant CRUD
    RestaurantResponseDTO createRestaurant(CreateRestaurantRequestDTO request);
    RestaurantResponseDTO getRestaurantById(Long restaurantId);
    List<RestaurantResponseDTO> getAllRestaurants();
    RestaurantResponseDTO updateRestaurant(Long restaurantId, UpdateRestaurantRequestDTO request);
    String deleteRestaurant(Long restaurantId);


    // Menu Item CRUD
    MenuItemResponseDTO addMenuItem(Long restaurantId, CreateMenuItemRequestDTO request);
    MenuItemResponseDTO updateMenuItem(Long restaurantId, Long menuItemId, UpdateMenuItemRequestDTO request);
    String deleteMenuItem(Long restaurantId, Long menuItemId);

    // Search Functionality
    List<RestaurantResponseDTO> searchRestaurantsByName(String name);
    List<MenuItemSearchResponseDTO> searchRestaurantsByMenuItem(String menuItemName);
}