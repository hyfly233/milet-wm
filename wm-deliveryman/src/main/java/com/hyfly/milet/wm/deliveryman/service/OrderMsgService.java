package com.hyfly.milet.wm.deliveryman.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.deliveryman.dao.DeliverymanDao;
import com.hyfly.milet.wm.deliveryman.dto.OrderMsgDto;
import com.hyfly.milet.wm.deliveryman.enums.DeliverymanStatus;
import com.hyfly.milet.wm.deliveryman.po.DeliverymanPo;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMsgService {

    @Autowired
    DeliverymanDao deliverymanDao;

    @Autowired
    ObjectMapper objectMapper;

    DeliverCallback deliverCallback = (consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            OrderMsgDto orderMsgDto = objectMapper.readValue(messageBody, OrderMsgDto.class);
            List<DeliverymanPo> deliverymanPoList = deliverymanDao.selectAvaliableDeliveryman(DeliverymanStatus.AVAILABLE.value);
            orderMsgDto.setDeliverymanId(deliverymanPoList.get(0).getId());
            log.info("onMessage:restaurantOrderMessageDTO:{}", orderMsgDto);

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMsgDto);
                channel.basicPublish("exchange.order.restaurant", "key.order", null, messageToSend.getBytes());
            }
        } catch (JsonProcessingException | TimeoutException e) {
            e.printStackTrace();
        }
    };

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare("exchange.order.deliveryman", BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare("queue.deliveryman", true, false, false, null);
            channel.queueBind("queue.deliveryman", "exchange.order.deliveryman", "key.deliveryman");

            channel.basicConsume("queue.deliveryman", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        }
    }
}

