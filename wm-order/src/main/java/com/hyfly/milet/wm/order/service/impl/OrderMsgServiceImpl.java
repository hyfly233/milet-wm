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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class OrderMsgServiceImpl implements OrderMsgService {

    public static final String EXCHANGE_ORDER_RESTAURANT = "exchange.order.restaurant";
    public static final String QUEUE_ORDER = "queue.order";
    public static final String KEY_ORDER = "key.order";
    public static final String EXCHANGE_ORDER_DELIVERYMAN = "exchange.order.deliveryman";

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
            channel.exchangeDeclare(EXCHANGE_ORDER_RESTAURANT, BuiltinExchangeType.DIRECT, true, false, null);

            channel.queueBind(QUEUE_ORDER, EXCHANGE_ORDER_DELIVERYMAN, KEY_ORDER);


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
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    });
}
