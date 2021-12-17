package com.hyfly.milet.wm.restaurant.po;

import com.hyfly.milet.wm.restaurant.enums.RestaurantStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class RestaurantPo {

    Integer id;

    String name;

    String address;

    /**
     * {@link RestaurantStatus}
     */
    String status;

    Long settlementId;

    Date date;
}
