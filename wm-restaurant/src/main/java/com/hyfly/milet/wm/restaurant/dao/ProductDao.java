package com.hyfly.milet.wm.restaurant.dao;

import com.hyfly.milet.wm.restaurant.po.ProductPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ProductDao {

    @Select("SELECT id,name,price,restaurant_id restaurantId,status,date FROM product WHERE id = #{id}")
    ProductPo selsctProduct(Integer id);
}
