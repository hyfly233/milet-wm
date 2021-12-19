package com.hyfly.milet.wm.deliveryman.po;

import com.hyfly.milet.wm.deliveryman.enums.DeliverymanStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class DeliverymanPo {
    private Integer id;
    private String name;
//    private String district;
    /**
     * {@link DeliverymanStatus}
     */
    private String status;
    private Date date;
}
