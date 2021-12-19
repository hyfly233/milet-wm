package com.hyfly.milet.wm.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.order.dao.OrderDetailDao;
import com.hyfly.milet.wm.order.dto.OrderMsgDto;
import com.hyfly.milet.wm.order.enums.OrderStatus;
import com.hyfly.milet.wm.order.po.OrderDetailPo;
import com.hyfly.milet.wm.order.service.OrderMsgService;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class OrderMsgServiceImpl implements OrderMsgService {

    public static final String EXCHANGE_ORDER_RESTAURANT = "exchange.order.restaurant";
    public static final String QUEUE_ORDER = "queue.order";
    public static final String KEY_ORDER = "key.order";
    public static final String EXCHANGE_ORDER_DELIVERYMAN = "exchange.order.deliveryman";
    public static final String EXCHANGE_ORDER_SETTLEMENT = "exchange.order.settlement";
    public static final String EXCHANGE_SETTLEMENT_ORDER = "exchange.settlement.order";
    public static final String EXCHANGE_ORDER_REWARD = "exchange.order.reward";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    OrderDetailDao orderDetailDao;

    @Override
    @Async
    public void handleMsg() throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            /* restaurant */
            channel.exchangeDeclare(EXCHANGE_ORDER_RESTAURANT, BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare(QUEUE_ORDER, true, false, false, null);
            channel.queueBind(QUEUE_ORDER, EXCHANGE_ORDER_RESTAURANT, KEY_ORDER);

            /* deliveryman */
            channel.exchangeDeclare(EXCHANGE_ORDER_DELIVERYMAN, BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueBind(QUEUE_ORDER, EXCHANGE_ORDER_DELIVERYMAN, KEY_ORDER);

            /* settlement */
            channel.exchangeDeclare(EXCHANGE_ORDER_SETTLEMENT, BuiltinExchangeType.FANOUT, true, false, null);
            channel.queueBind(QUEUE_ORDER, EXCHANGE_SETTLEMENT_ORDER, KEY_ORDER);

            /* reward */
            channel.exchangeDeclare(EXCHANGE_ORDER_REWARD, BuiltinExchangeType.TOPIC, true, false, null);
            channel.queueBind(QUEUE_ORDER, EXCHANGE_ORDER_REWARD, KEY_ORDER);

            channel.basicConsume(QUEUE_ORDER, true, deliverCallback, consumerTag -> {
            });

            while (true) {
                Thread.sleep(1000000);
            }
        }

    }

    DeliverCallback deliverCallback = ((tag, msg) -> {
        String msgBody = new String(msg.getBody());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try {
            OrderMsgDto orderMsgDto = objectMapper.readValue(msgBody, OrderMsgDto.class);
            OrderDetailPo orderDetailPo = orderDetailDao.selectOrder(orderMsgDto.getOrderId());

            if (OrderStatus.ORDER_CREATING.value.equals(orderDetailPo.getStatus())) {
                if (orderMsgDto.getConfirmed() != null && orderMsgDto.getPrice() != null) {
                    orderDetailPo.setStatus(OrderStatus.RESTAURANT_CONFIRMED.value);
                    orderDetailPo.setPrice(orderMsgDto.getPrice());

                    orderDetailDao.update(orderDetailPo);

                    try (Connection connection = connectionFactory.newConnection();
                         Channel channel = connection.createChannel()) {

                        String s = objectMapper.writeValueAsString(orderMsgDto);

                        channel.basicPublish(EXCHANGE_ORDER_DELIVERYMAN, "key.deliveryman", null, s.getBytes());
                    }
                } else {
                    orderDetailPo.setStatus(OrderStatus.FAILED.value);
                    orderDetailDao.update(orderDetailPo);
                }
            } else if (OrderStatus.RESTAURANT_CONFIRMED.value.equals(orderDetailPo.getStatus())) {
                if (orderMsgDto.getDeliverymanId() != null) {
                    orderDetailPo.setStatus(OrderStatus.DELIVERYMAN_CONFIRMED.value);
                    orderDetailPo.setDeliverymanId(orderMsgDto.getDeliverymanId());

                    orderDetailDao.update(orderDetailPo);

                    try (Connection connection = connectionFactory.newConnection();
                         Channel channel = connection.createChannel()) {

                        String s = objectMapper.writeValueAsString(orderMsgDto);

                        channel.basicPublish(EXCHANGE_ORDER_SETTLEMENT, "key.settlement", null, s.getBytes());
                    }
                } else {
                    orderDetailPo.setStatus(OrderStatus.FAILED.value);
                    orderDetailDao.update(orderDetailPo);
                }
            } else if (OrderStatus.DELIVERYMAN_CONFIRMED.value.equals(orderDetailPo.getStatus())) {
                if (orderMsgDto.getSettlementId() != null) {
                    orderDetailPo.setStatus(OrderStatus.SETTLEMENT_CONFIRMED.value);
                    orderDetailPo.setSettlementId(orderMsgDto.getSettlementId());

                    orderDetailDao.update(orderDetailPo);

                    try (Connection connection = connectionFactory.newConnection();
                         Channel channel = connection.createChannel()) {

                        String s = objectMapper.writeValueAsString(orderMsgDto);

                        channel.basicPublish(EXCHANGE_ORDER_REWARD, "key.reward", null, s.getBytes());
                    }
                } else {
                    orderDetailPo.setStatus(OrderStatus.FAILED.value);
                    orderDetailDao.update(orderDetailPo);
                }
            } else if (OrderStatus.SETTLEMENT_CONFIRMED.value.equals(orderDetailPo.getStatus())) {
                if (orderMsgDto.getRewardId() != null) {
                    orderDetailPo.setStatus(OrderStatus.ORDER_CREATED.value);
                    orderDetailPo.setRewardId(orderMsgDto.getRewardId());
                } else {
                    orderDetailPo.setStatus(OrderStatus.FAILED.value);
                }
                orderDetailDao.update(orderDetailPo);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    });
}
