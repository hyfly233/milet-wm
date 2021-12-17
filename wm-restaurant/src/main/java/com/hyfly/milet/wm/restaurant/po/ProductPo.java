package com.hyfly.milet.wm.restaurant.po;

import com.hyfly.milet.wm.restaurant.enums.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
public class ProductPo {

    Integer id;

    String name;

    BigDecimal price;

    Integer restaurantId;

    /**
     * {@link ProductStatus}
     */
    String status;

    Date date;
}
