package com.hyfly.milet.wm.deliveryman.dao;

import com.hyfly.milet.wm.deliveryman.enums.DeliverymanStatus;
import com.hyfly.milet.wm.deliveryman.po.DeliverymanPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DeliverymanDao {

    @Select("SELECT id,name,status,date FROM deliveryman WHERE id = #{id}")
    DeliverymanPo selectDeliveryman(Integer id);

    @Select("SELECT id,name,status,date FROM deliveryman WHERE status = #{status}")
    List<DeliverymanPo> selectAvaliableDeliveryman(String status);
}
