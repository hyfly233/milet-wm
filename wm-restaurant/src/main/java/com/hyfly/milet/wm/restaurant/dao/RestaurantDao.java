package com.hyfly.milet.wm.restaurant.dao;

import com.hyfly.milet.wm.restaurant.po.RestaurantPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RestaurantDao {

    @Select("SELECT id,name,address,status,settlement_id settlementId,date FROM restaurant WHERE id = #{id}")
    RestaurantPo selsctRestaurant(Integer id);
}
