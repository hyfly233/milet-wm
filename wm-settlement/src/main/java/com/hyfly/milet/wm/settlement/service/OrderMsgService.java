package com.hyfly.milet.wm.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyfly.milet.wm.settlement.dao.SettlementDao;
import com.hyfly.milet.wm.settlement.dto.OrderMsgDto;
import com.hyfly.milet.wm.settlement.enums.SettlementStatus;
import com.hyfly.milet.wm.settlement.po.SettlementPo;
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

    public static final String EXCHANGE_ORDER_SETTLEMENT = "exchange.order.settlement";
    public static final String EXCHANGE_SETTLEMENT_ORDER = "exchange.settlement.order";

    @Autowired
    SettlementDao settlementDao;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SettlementService settlementService;

    DeliverCallback deliverCallback = (consumerTag, message) -> {
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            OrderMsgDto orderMsgDto = objectMapper.readValue(messageBody, OrderMsgDto.class);
            log.info("handleOrderService:orderSettlementDTO:{}", orderMsgDto);
            SettlementPo settlementPo = new SettlementPo();
            settlementPo.setAmount(orderMsgDto.getPrice());
            settlementPo.setDate(new Date());
            settlementPo.setOrderId(orderMsgDto.getOrderId());
            settlementPo.setStatus(SettlementStatus.SUCCESS.value);
            settlementPo.setTransactionId(settlementService.settlement(orderMsgDto.getAccountId(), orderMsgDto.getPrice()));
            settlementDao.insert(settlementPo);
            orderMsgDto.setSettlementId(settlementPo.getId());
            log.info("handleOrderService:settlementOrderDTO:{}", orderMsgDto);

            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMsgDto);
                channel.basicPublish(EXCHANGE_SETTLEMENT_ORDER, "key.order", null, messageToSend.getBytes());
            }
        } catch (JsonProcessingException | TimeoutException e) {
            e.printStackTrace();
        }
    };

    @Async
    public void handleMsg() throws IOException, TimeoutException, InterruptedException {
        log.info("start linstening message");
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setHost("localhost");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_SETTLEMENT_ORDER, BuiltinExchangeType.FANOUT, true, false, null);
            channel.queueDeclare("queue.order", true, false, false, null);
            channel.queueBind("queue.settlement", EXCHANGE_ORDER_SETTLEMENT, "key.settlement");

            channel.basicConsume("queue.settlement", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(100000);
            }
        }
    }
}

