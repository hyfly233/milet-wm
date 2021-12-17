package com.hyfly.milet.wm.order.config;

import com.hyfly.milet.wm.order.service.OrderMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class RabbitConfig {

    @Autowired
    OrderMsgService orderMsgService;

    @Autowired
    public void startListenMsg() throws IOException, InterruptedException, TimeoutException {
        orderMsgService.handleMsg();
    }
}
