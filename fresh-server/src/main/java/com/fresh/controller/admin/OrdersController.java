package com.fresh.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fresh.dto.OrdersPageQueryDTO;
import com.fresh.entity.Orders;
import com.fresh.exception.OrderStatusException;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.OrdersService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private RedissonClient redissonClient;

    @GetMapping("/page")
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO){

        return ordersService.page(ordersPageQueryDTO);
    }

    @GetMapping("/comfirm")
    public Result comfirm(Long id){
        LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
        uw.eq(Orders::getId,id);
        uw.eq(Orders::getStatus,2);
        uw.set(Orders::getStatus,3);

        ordersService.update(uw);
        return Result.success();
    }

    @GetMapping("/delivery")
    public Result delivery(Long id){
        LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
        uw.eq(Orders::getId,id);
        uw.eq(Orders::getStatus,3);
        uw.set(Orders::getStatus,4);

        ordersService.update(uw);
        return Result.success();
    }

    @GetMapping("/finish")
    public Result finish(Long id){
        LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
        uw.eq(Orders::getId,id);
        uw.eq(Orders::getStatus,4);
        uw.set(Orders::getStatus,5);
        uw.set(Orders::getDeliveryTime,LocalDateTime.now());

        ordersService.update(uw);
        return Result.success();
    }

    @GetMapping("/cancel")
    public Result cancel(Long id){
        LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
        uw.eq(Orders::getId,id);
        uw.eq(Orders::getStatus,1);
        uw.set(Orders::getStatus,6);
        uw.set(Orders::getCancelReason,"商家取消订单");
        uw.set(Orders::getCancelTime, LocalDateTime.now());

        boolean row = ordersService.update(uw);
        if(row==false){
            throw new OrderStatusException("只有待支付的订单可以取消");
        }
        return Result.success();
    }


}
