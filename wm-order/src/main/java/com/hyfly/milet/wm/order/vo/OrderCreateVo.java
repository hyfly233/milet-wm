package com.hyfly.milet.wm.order.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreateVo {

    Integer accountId;

    String address;

    Integer productId;
}
