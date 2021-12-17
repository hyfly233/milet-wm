package com.hyfly.milet.wm.restaurant.enums;

public enum RestaurantStatus {

    /**
     * closed
     */
    CLOSED("closed"),

    IN_OPERATION("in_operation");

    public final String value;

    public String getValue() {
        return value;
    }

    RestaurantStatus(String value) {
        this.value = value;
    }
}
