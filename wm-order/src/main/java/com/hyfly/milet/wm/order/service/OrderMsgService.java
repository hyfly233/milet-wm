package com.hyfly.milet.wm.order.service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface OrderMsgService {

    /**
     * 声明消息队列、交换机、绑定、消息处理
     */
    void handleMsg() throws IOException, TimeoutException, InterruptedException;

}
