package com.hyfly.milet.wm.restaurant.enums;

public enum ProductStatus {

    /**
     *
     */
    AVAILABLE("available"),

    NOT_AVAILABLE("not_available");

    public final String value;

    public String getValue() {
        return value;
    }

    ProductStatus(String value) {
        this.value = value;
    }
}
