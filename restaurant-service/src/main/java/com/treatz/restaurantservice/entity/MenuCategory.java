package com.treatz.restaurantservice.entity;

public enum MenuCategory {
    STARTER("Starter"),
    MAIN_COURSE("Main Course"),
    DESSERT("Dessert"),
    BEVERAGE("Beverage"),
    SIDE_DISH("Side Dish"),
    APPETIZER("Appetizer");

    private final String displayName;

    MenuCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}