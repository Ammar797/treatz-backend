package com.treatz.restaurantservice.service;

import com.treatz.restaurantservice.dto.*;
import com.treatz.restaurantservice.entity.MenuItem;
import com.treatz.restaurantservice.entity.Restaurant;
import com.treatz.restaurantservice.exception.MenuItemNotBelongsToRestaurantException;
import com.treatz.restaurantservice.exception.ResourceNotFoundException;
import com.treatz.restaurantservice.mapper.RestaurantMapper;
import com.treatz.restaurantservice.repository.MenuItemRepository;
import com.treatz.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantMapper restaurantMapper;

    // == RESTAURANT LOGIC ==

    @Override
    public RestaurantResponseDTO createRestaurant(CreateRestaurantRequestDTO request) {
        Restaurant restaurant = restaurantMapper.createRequestToRestaurant(request);
        restaurant.setOwnerId(getAuthenticatedUserId());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.restaurantToResponseDTO(savedRestaurant);
    }

    @Override
    public RestaurantResponseDTO updateRestaurant(Long restaurantId, UpdateRestaurantRequestDTO request) {
        Restaurant restaurant = findRestaurantAndVerifyOwnership(restaurantId);
        restaurantMapper.updateRestaurantFromDto(request, restaurant); // Use mapper to update fields
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.restaurantToResponseDTO(updatedRestaurant);
    }

    @Override
    public String deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = findRestaurantAndVerifyOwnership(restaurantId);
        restaurantRepository.delete(restaurant);
        return "Restaurant with ID " + restaurantId + " deleted successfully.";
    }

    @Override
    public List<MenuItemResponseDTO> getMenuItemsByIds(List<Long> ids) {
        List<MenuItem> menuItems = menuItemRepository.findByIdIn(ids);
        return restaurantMapper.menuItemsToMenuItemResponseDTO(menuItems);
    }

    @Override
    public RestaurantResponseDTO getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.restaurantToResponseDTO(restaurant);
    }

    @Override
    public List<RestaurantSummaryDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(restaurantMapper::restaurantToSummaryDTO)
                .collect(Collectors.toList());
    }

    // == MENU ITEM LOGIC ==

    @Override
    public MenuItemResponseDTO addMenuItem(Long restaurantId, CreateMenuItemRequestDTO request) {
        Restaurant restaurant = findRestaurantAndVerifyOwnership(restaurantId);
        MenuItem menuItem = restaurantMapper.createMenuItemRequestToMenuItem(request);
        menuItem.setRestaurant(restaurant);
        MenuItem savedItem = menuItemRepository.save(menuItem);
        return restaurantMapper.menuItemToResponseDTO(savedItem);
    }

    @Override
    public MenuItemResponseDTO updateMenuItem(Long restaurantId, Long menuItemId, UpdateMenuItemRequestDTO request) {
        Restaurant restaurant = findRestaurantAndVerifyOwnership(restaurantId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));

        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
            throw new MenuItemNotBelongsToRestaurantException(
                    String.format("Menu item with ID %d does not belong to restaurant with ID %d",
                            menuItemId, restaurantId));
        }

        restaurantMapper.updateMenuItemFromDto(request, menuItem);
        MenuItem updatedItem = menuItemRepository.save(menuItem);
        return restaurantMapper.menuItemToResponseDTO(updatedItem);
    }

    @Override
    public String deleteMenuItem(Long restaurantId, Long menuItemId) {
        Restaurant restaurant = findRestaurantAndVerifyOwnership(restaurantId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + menuItemId));

        if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
            throw new MenuItemNotBelongsToRestaurantException(
                    String.format("Menu item with ID %d does not belong to restaurant with ID %d",
                            menuItemId, restaurantId));
        }
        menuItemRepository.delete(menuItem);
        return "Menu item with ID " + menuItemId + " deleted successfully.";
    }

    @Override
    public Long getOwnerIdForRestaurant(Long restaurantId) {
        // Find the restaurant by its ID
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                // If it's not found, throw our standard exception
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        // Return just the ownerId from the entity
        return restaurant.getOwnerId();
    }

    // == SEARCH LOGIC ==

    @Override
    public List<RestaurantSummaryDTO> searchRestaurantsByName(String name) {
        return restaurantRepository.findByNameContainingIgnoreCase(name).stream()
                .map(restaurantMapper::restaurantToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuItemSearchResponseDTO> searchRestaurantsByMenuItem(String menuItemName) {
        return menuItemRepository.findByNameContainingIgnoreCase(menuItemName).stream()
                .map(restaurantMapper::menuItemToSearchResponseDTO)
                .collect(Collectors.toList());
    }

    // == PAGINATED METHODS ==

    @Override
    public Page<RestaurantSummaryDTO> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAll(pageable)
                .map(restaurantMapper::restaurantToSummaryDTO);
    }

    @Override
    public Page<RestaurantSummaryDTO> searchRestaurantsByName(String name, Pageable pageable) {
        return restaurantRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(restaurantMapper::restaurantToSummaryDTO);
    }

    @Override
    public Page<MenuItemSearchResponseDTO> searchRestaurantsByMenuItem(String menuItemName, Pageable pageable) {
        return menuItemRepository.findByNameContainingIgnoreCase(menuItemName, pageable)
                .map(restaurantMapper::menuItemToSearchResponseDTO);
    }

    // == HELPER METHODS ==

    private Long getAuthenticatedUserId() {
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getClaim("userId");
    }

    private Restaurant findRestaurantAndVerifyOwnership(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        if (!restaurant.getOwnerId().equals(getAuthenticatedUserId())) {
            throw new AccessDeniedException("User is not authorized to modify this restaurant.");
        }
        return restaurant;
    }
}