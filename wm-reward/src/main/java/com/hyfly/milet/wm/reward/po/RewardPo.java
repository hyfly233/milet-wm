package com.hyfly.milet.wm.reward.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RewardPo {
    private Integer id;
    private Integer orderId;
    private BigDecimal amount;

    /**
     * {@link com.hyfly.milet.wm.reward.enums.RewardStatus}
     */
    private String status;
    private Date date;
}
