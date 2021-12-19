package com.hyfly.milet.wm.reward.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.reward.dao.RewardDao;
import com.hyfly.milet.wm.reward.dto.OrderMsgDto;
import com.hyfly.milet.wm.reward.enums.RewardStatus;
import com.hyfly.milet.wm.reward.po.RewardPo;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMsgService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RewardDao rewardDao;

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare("exchange.order.reward", BuiltinExchangeType.TOPIC, true, false, null);
            channel.queueDeclare("queue.reward", true, false, false, null);
            channel.queueBind("queue.reward", "exchange.order.reward", "key.reward");

            channel.basicConsume("queue.reward", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        }
    }

    DeliverCallback deliverCallback = (consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            OrderMsgDto orderMsgDto = objectMapper.readValue(messageBody, OrderMsgDto.class);

            RewardPo rewardPo = new RewardPo();

            rewardPo.setOrderId(orderMsgDto.getOrderId());
            rewardPo.setStatus(RewardStatus.SUCCESS.value);
            rewardPo.setAmount(orderMsgDto.getPrice());
            rewardPo.setDate(new Date());

            rewardDao.insert(rewardPo);

            orderMsgDto.setRewardId(rewardPo.getId());

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMsgDto);
                channel.basicPublish("exchange.order.reward", "key.order", null, messageToSend.getBytes());
            }

        } catch (JsonProcessingException | TimeoutException e) {
            e.printStackTrace();
        }
    };
}

