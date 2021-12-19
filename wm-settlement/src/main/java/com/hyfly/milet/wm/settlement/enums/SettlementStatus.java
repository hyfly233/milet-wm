package com.hyfly.milet.wm.settlement.enums;

public enum SettlementStatus {

    /**
     * success
     */
    SUCCESS("success"),
    FAILED("failed");

    public final String value;

    public String getValue() {
        return value;
    }

    SettlementStatus(String value) {
        this.value = value;
    }
}
