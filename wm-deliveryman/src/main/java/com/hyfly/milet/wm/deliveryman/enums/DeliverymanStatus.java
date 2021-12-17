package com.hyfly.milet.wm.deliveryman.enums;

public enum DeliverymanStatus {
    /**
     *
     */
    AVAILABLE("available"),

    NOT_AVAILABLE("not_available");

    public final String value;

    public String getValue() {
        return value;
    }

    DeliverymanStatus(String value) {
        this.value = value;
    }
}
