package com.hyfly.milet.wm.settlement.dto;

import com.hyfly.milet.wm.settlement.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderMsgDto {

    Integer orderId;

    /**
     * {@link OrderStatus}
     */
    String orderStatus;

    BigDecimal price;

    Integer deliverymanId;

    Integer productId;

    Integer accountId;

    Integer settlementId;

    Integer rewardId;

    BigDecimal rewardAmount;

    Boolean confirmed;
}
