package com.hyfly.milet.wm.order.po;


import com.hyfly.milet.wm.order.vo.OrderCreateVo;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * order_detal 表对应的Entity
 */
@Data
@Builder
public class OrderDetailPo {

    Integer id;

    /**
     * {@link com.hyfly.milet.wm.order.enums.OrderStatus}
     */
    String status;

    String address;

    Integer accountId;

    Integer productId;

    Integer deliverymanId;

    Integer settlementId;

    Integer rewardId;

    BigDecimal price;

    Date date;

    public static OrderDetailPo convertViaVo(OrderCreateVo vo) {
        return OrderDetailPo.builder()
                .address(vo.getAddress())
                .accountId(vo.getAccountId())
                .productId(vo.getProductId())
                .build();
    }
}
