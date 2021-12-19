package com.hyfly.milet.wm.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.order.dao.OrderDetailDao;
import com.hyfly.milet.wm.order.dto.OrderMsgDto;
import com.hyfly.milet.wm.order.enums.OrderStatus;
import com.hyfly.milet.wm.order.po.OrderDetailPo;
import com.hyfly.milet.wm.order.service.OrderService;
import com.hyfly.milet.wm.order.vo.OrderCreateVo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDetailDao orderDetailDao;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void createOrder(OrderCreateVo createVo) throws IOException, TimeoutException, InterruptedException {
        OrderDetailPo orderDetailPo = OrderDetailPo.convertViaVo(createVo);

        orderDetailPo.setStatus(OrderStatus.ORDER_CREATING.value);
        orderDetailPo.setDate(new Date());

        orderDetailDao.insert(orderDetailPo);

        OrderMsgDto msgDto = OrderMsgDto.convertViaDetailPo(orderDetailPo);

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.confirmSelect();

            String msgJsonToSend = objectMapper.writeValueAsString(msgDto);

            // 单个消息的 ttl
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
//                    .expiration("150000")
                    .build();
            channel.basicPublish("exchange.order.restaurant", "key.restaurant", properties, msgJsonToSend.getBytes());

            log.info("message sent");

            if (channel.waitForConfirms()) {
                log.info("mq confirm success");
            } else {
                log.info("mq confirm failed");

                orderDetailPo.setStatus(OrderStatus.FAILED.value);
                orderDetailDao.update(orderDetailPo);
            }
        }
    }
}
