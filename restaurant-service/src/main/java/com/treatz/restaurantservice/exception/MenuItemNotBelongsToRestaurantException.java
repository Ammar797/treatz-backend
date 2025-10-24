package com.treatz.restaurantservice.exception;

public class MenuItemNotBelongsToRestaurantException extends RuntimeException {
    public MenuItemNotBelongsToRestaurantException(String message) {
        super(message);
    }
}