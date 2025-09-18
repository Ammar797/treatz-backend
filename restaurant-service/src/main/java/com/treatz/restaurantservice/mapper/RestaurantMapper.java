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
    Restaurant createRequestToRestaurant(CreateRestaurantRequestDTO dto);

    // Maps the database entity to the public-facing DTO
    RestaurantResponseDTO restaurantToResponseDTO(Restaurant restaurant);

    // Maps a list of entities to a list of DTOs
    List<RestaurantResponseDTO> restaurantsToResponseDTOs(List<Restaurant> restaurants);

    // Updates an existing restaurant entity from a DTO's data
    void updateRestaurantFromDto(UpdateRestaurantRequestDTO dto, @MappingTarget Restaurant restaurant);


    // --- Menu Item Mappings ---

    // In RestaurantMapper.java
    MenuItem createMenuItemRequestToMenuItem(CreateMenuItemRequestDTO dto);;

    MenuItemResponseDTO menuItemToResponseDTO(MenuItem menuItem);

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