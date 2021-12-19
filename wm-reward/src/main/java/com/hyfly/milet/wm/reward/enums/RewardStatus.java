package com.hyfly.milet.wm.reward.enums;

public enum RewardStatus {

    /**
     * success
     */
    SUCCESS("success"),
    FAILED("failed");

    public final String value;

    public String getValue() {
        return value;
    }

    RewardStatus(String value) {
        this.value = value;
    }
}
