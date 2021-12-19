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
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OrderMsgService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ProductDao productDao;

    @Autowired
    RestaurantDao restaurantDao;

    @Autowired
    Channel rabbitChannel;

    @Async
    public void handleMessage() throws IOException, InterruptedException {
        log.info("start linstening message");

        /* dlx */
        rabbitChannel.exchangeDeclare("exchange.dlx", BuiltinExchangeType.TOPIC, true, false, null);
        rabbitChannel.queueDeclare("queue.dlx", true, false, false, null);
        rabbitChannel.queueBind("queue.dlx", "exchange.dlx", "#");

        rabbitChannel.exchangeDeclare("exchange.order.restaurant", BuiltinExchangeType.DIRECT, true, false, null);

        Map<String, Object> args = new HashMap<>(16);
        args.put("x-message-ttl", 10000);
        args.put("x-dead-letter-exchange", "exchange.dlx");
        args.put("x-max-length", 10);
        rabbitChannel.queueDeclare("queue.restaurant", true, false, false, args);

        rabbitChannel.queueBind("queue.restaurant", "exchange.order.restaurant", "key.restaurant");

        rabbitChannel.basicQos(2);
        // 取消自动 ACK
        rabbitChannel.basicConsume("queue.restaurant", false,
                (consumerTag, message) -> {
                    String messageBody = new String(message.getBody());
                    log.info("deliverCallback:messageBody:{}", messageBody);

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

                        // 手动签收单条 ack
                        rabbitChannel.basicAck(message.getEnvelope().getDeliveryTag(), false);
//                        rabbitChannel.basicNack(message.getEnvelope().getDeliveryTag(), false, true);

                        // 消息返回机制
                        rabbitChannel.addReturnListener(returnMessage -> log.info("Msg return: [{}]", returnMessage));

                        String messageToSend = objectMapper.writeValueAsString(orderMsgDto);
                        rabbitChannel.basicPublish("exchange.order.restaurant", "key.order", true, null, messageToSend.getBytes());

                        Thread.sleep(1000);

                    } catch (JsonProcessingException | InterruptedException e) {
                        e.printStackTrace();
                    }
                },
                consumerTag -> {
                });

        while (true) {
            Thread.sleep(100000);
        }

    }
}

