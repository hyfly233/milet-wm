package com.hyfly.milet.wm.settlement.enums;

public enum OrderStatus {

    /**
     * order_creating
     */
    ORDER_CREATING("order_creating"),

    /**
     * restaurant_confirmed
     */
    RESTAURANT_CONFIRMED("restaurant_confirmed"),

    /**
     * deliveryman_confirmed
     */
    DELIVERYMAN_CONFIRMED("deliveryman_confirmed"),

    /**
     * settlement_confirmed
     */
    SETTLEMENT_CONFIRMED("settlement_confirmed"),

    /**
     * order_created
     */
    ORDER_CREATED("order_created"),

    /**
     * failed
     */
    FAILED("failed");

    public final String value;

    public String getValue() {
        return value;
    }

    OrderStatus(String value) {
        this.value = value;
    }
}