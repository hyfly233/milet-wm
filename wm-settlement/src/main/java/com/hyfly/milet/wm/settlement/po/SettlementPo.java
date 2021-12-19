package com.hyfly.milet.wm.settlement.po;

import com.hyfly.milet.wm.settlement.enums.SettlementStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SettlementPo {
    private Integer id;
    private Integer orderId;
    private Integer transactionId;

    /**
     * {@link SettlementStatus}
     */
    private String status;
    private BigDecimal amount;
    private Date date;
}
