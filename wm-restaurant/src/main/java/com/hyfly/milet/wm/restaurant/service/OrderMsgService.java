package com.hyfly.milet.wm.restaurant.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.restaurant.dao.ProductDao;
import com.hyfly.milet.wm.restaurant.dao.RestaurantDao;
import com.hyfly.milet.wm.restaurant.dto.OrderMsgDto;
import com.hyfly.milet.wm.restaurant.enums.ProductStatus;
import com.hyfly.milet.wm.restaurant.enums.RestaurantStatus;
import com.hyfly.milet.wm.restaurant.po.ProductPo;
import com.hyfly.milet.wm.restaurant.po.RestaurantPo;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMsgService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductDao productDao;

    @Autowired
    RestaurantDao restaurantDao;

    @Async
    public void handleMessage() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare("exchange.order.restaurant", BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare("queue.restaurant", true, false, false, null);
            channel.queueBind("queue.restaurant", "exchange.order.restaurant", "key.restaurant");

            channel.basicConsume("queue.restaurant", true, deliverCallback, consumerTag -> {
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

            ProductPo productPo = productDao.selsctProduct(orderMsgDto.getProductId());
            log.info("onMessage:ProductPo:{}", productPo);
            RestaurantPo restaurantPo = restaurantDao.selsctRestaurant(productPo.getRestaurantId());
            log.info("onMessage:RestaurantPo:{}", restaurantPo);
            if (ProductStatus.AVAILABLE.value.equals(productPo.getStatus()) && RestaurantStatus.IN_OPERATION.value.equals(restaurantPo.getStatus())) {
                orderMsgDto.setConfirmed(true);
                orderMsgDto.setPrice(productPo.getPrice());
            } else {
                orderMsgDto.setConfirmed(false);
            }
            log.info("sendMessage:restaurantOrderMsgDto:{}", orderMsgDto);

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMsgDto);
                channel.basicPublish("exchange.order.restaurant", "key.order", null, messageToSend.getBytes());
            }

        } catch (JsonProcessingException | TimeoutException e) {
            e.printStackTrace();
        }
    };
}

