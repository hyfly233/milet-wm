package com.hyfly.milet.wm.order.dto;

import com.hyfly.milet.wm.order.enums.OrderStatus;
import com.hyfly.milet.wm.order.po.OrderDetailPo;
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

    public static OrderMsgDto convertViaDetailPo(OrderDetailPo po) {
        return OrderMsgDto.builder()
                .orderId(po.getId())
                .orderStatus(po.getStatus())
                .price(po.getPrice())
                .deliverymanId(po.getDeliverymanId())
                .productId(po.getProductId())
                .accountId(po.getAccountId())
                .rewardId(po.getRewardId())
                .build();
    }
}
