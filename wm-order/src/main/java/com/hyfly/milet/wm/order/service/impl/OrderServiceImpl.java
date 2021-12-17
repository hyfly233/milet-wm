package com.hyfly.milet.wm.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.order.dao.OrderDetailDao;
import com.hyfly.milet.wm.order.dto.OrderMsgDto;
import com.hyfly.milet.wm.order.enums.OrderStatus;
import com.hyfly.milet.wm.order.po.OrderDetailPo;
import com.hyfly.milet.wm.order.service.OrderService;
import com.hyfly.milet.wm.order.vo.OrderCreateVo;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderDetailDao orderDetailDao;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public void createOrder(OrderCreateVo createVo) throws IOException, TimeoutException {
        OrderDetailPo orderDetailPo = OrderDetailPo.convertViaVo(createVo);

        orderDetailPo.setStatus(OrderStatus.ORDER_CREATING.value);
        orderDetailPo.setDate(new Date());

        orderDetailDao.insert(orderDetailPo);

        OrderMsgDto msgDto = OrderMsgDto.convertViaDetailPo(orderDetailPo);

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            String msgJsonToSend = objectMapper.writeValueAsString(msgDto);

            channel.basicPublish("exchange.order.restaurant", "key.restaurant", null, msgJsonToSend.getBytes());
        }
    }
}
