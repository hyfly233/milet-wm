package com.hyfly.milet.wm.restaurant.dto;

import com.hyfly.milet.wm.restaurant.enums.OrderStatus;
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

//    public static OrderMsgDto convertViaDetailPo(OrderDetailPo po) {
//        return OrderMsgDto.builder()
//                .orderId(po.getId())
//                .orderStatus(po.getStatus())
//                .price(po.getPrice())
//                .deliverymanId(po.getDeliverymanId())
//                .productId(po.getProductId())
//                .accountId(po.getAccountId())
//                .rewardId(po.getRewardId())
//                .build();
//    }
}
