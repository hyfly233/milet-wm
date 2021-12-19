package com.hyfly.milet.wm.order.service;

import com.hyfly.milet.wm.order.vo.OrderCreateVo;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface OrderService {

    void createOrder(OrderCreateVo createVo) throws IOException, TimeoutException, InterruptedException;
}
