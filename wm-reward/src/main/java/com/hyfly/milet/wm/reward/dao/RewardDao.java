package com.hyfly.milet.wm.reward.dao;

import com.hyfly.milet.wm.reward.po.RewardPo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RewardDao {

    @Insert("INSERT INTO reward (order_id, amount, status, date) VALUES(#{orderId}, #{amount}, #{status}, #{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RewardPo rewardPo);
}
