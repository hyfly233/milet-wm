package com.hyfly.milet.wm.order.controller;

import com.hyfly.milet.wm.order.service.OrderService;
import com.hyfly.milet.wm.order.vo.OrderCreateVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVo vo) throws IOException, TimeoutException, InterruptedException {
        log.info("OrderController:createOrder ---- start");
        orderService.createOrder(vo);
    }
}
