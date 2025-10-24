package com.treatz.restaurantservice.mapper;

import com.treatz.restaurantservice.dto.*;
import com.treatz.restaurantservice.entity.MenuItem;
import com.treatz.restaurantservice.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    // --- Restaurant Mappings ---

    // Maps the public-facing DTO to the database entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Restaurant createRequestToRestaurant(CreateRestaurantRequestDTO dto);

    // Maps the database entity to the public-facing DTO (includes menu items)
    RestaurantResponseDTO restaurantToResponseDTO(Restaurant restaurant);

    // Maps the database entity to summary DTO (no menu items - for list/search)
    RestaurantSummaryDTO restaurantToSummaryDTO(Restaurant restaurant);

    // Maps a list of entities to a list of DTOs
    List<RestaurantResponseDTO> restaurantsToResponseDTOs(List<Restaurant> restaurants);

    // Maps a list of entities to a list of summary DTOs
    List<RestaurantSummaryDTO> restaurantsToSummaryDTOs(List<Restaurant> restaurants);

    // Updates an existing restaurant entity from a DTO's data
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateRestaurantFromDto(UpdateRestaurantRequestDTO dto, @MappingTarget Restaurant restaurant);


    // --- Menu Item Mappings ---

    // Maps create request DTO to MenuItem entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MenuItem createMenuItemRequestToMenuItem(CreateMenuItemRequestDTO dto);

    MenuItemResponseDTO menuItemToResponseDTO(MenuItem menuItem);

    // Updates existing menu item from update request DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateMenuItemFromDto(UpdateMenuItemRequestDTO dto, @MappingTarget MenuItem menuItem);


    // --- Search Mappings (The Important Fix!) ---

    // This specifically maps a MenuItem entity to our search result DTO
    // It knows to use the method below to handle the nested restaurant object!
    @Mapping(source = "restaurant", target = "restaurant")
    MenuItemSearchResponseDTO menuItemToSearchResponseDTO(MenuItem menuItem);

    // This is a helper mapping for the search DTO, creating the mini restaurant profile
    RestaurantInfoDTO restaurantToRestaurantInfoDTO(Restaurant restaurant);

    List<MenuItemResponseDTO> menuItemsToMenuItemResponseDTO(List<MenuItem> menuItems);
}