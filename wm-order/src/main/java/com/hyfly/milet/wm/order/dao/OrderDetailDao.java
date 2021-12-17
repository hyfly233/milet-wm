package com.hyfly.milet.wm.order.dao;

import com.hyfly.milet.wm.order.po.OrderDetailPo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderDetailDao {

    @Insert("INSERT INTO order_detail (status, address, account_id, product_id, deliveryman_id, settlement_id, " +
            "reward_id, price, date) VALUES(#{status}, #{address},#{accountId},#{productId},#{deliverymanId}," +
            "#{settlementId}, #{rewardId},#{price}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OrderDetailPo OrderDetailPo);

    @Update("update order_detail set status =#{status}, address =#{address}, account_id =#{accountId}, " +
            "product_id =#{productId}, deliveryman_id =#{deliverymanId}, settlement_id =#{settlementId}, " +
            "reward_id =#{rewardId}, price =#{price}, date =#{date} where id=#{id}")
    void update(OrderDetailPo OrderDetailPo);

    @Select("SELECT id,status,address,account_id accountId, product_id productId,deliveryman_id deliverymanId," +
            "settlement_id settlementId,reward_id rewardId,price, date FROM order_detail WHERE id = #{id}")
    OrderDetailPo selectOrder(Integer id);
}
