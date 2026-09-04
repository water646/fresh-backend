package com.fresh.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fresh.context.BaseContext;
import com.fresh.dto.OrdersPageQueryDTO;
import com.fresh.dto.OrdersPaymentDTO;
import com.fresh.dto.OrdersSubmitDTO;
import com.fresh.entity.Orders;
import com.fresh.exception.OrderStatusException;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.OrdersService;
import com.fresh.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController("userOrdersController")
@RequestMapping("/user/orders")
@Api(tags = "C端订单相关接口")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private RedissonClient redissonClient;

    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result submitOrder(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        //返回订单（含自增 id 与订单号 number），前端支付要 orderNumber、查详情要 id
        return Result.success(ordersService.submitOrder(ordersSubmitDTO));
    }

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}",ordersPaymentDTO);

        //用redisson锁，防误删、自动续期、支持阻塞等待、可重入
        RLock lock = redissonClient.getLock("payment_lock:"+ordersPaymentDTO.getOrderNumber());
        if(!lock.tryLock()){
            return Result.error("请勿重复支付");
        }

        //拿到锁之后，执行支付逻辑
        try{
            ordersService.paySuccess(ordersPaymentDTO.getOrderNumber());
        }finally {
            lock.unlock();
        }

        return Result.success();
    }

    /**
     * 查询订单详情（订单基本信息 + 订单明细列表）
     * @param id 订单id
     * @return 订单详情 VO
     */
    @GetMapping("/detail")
    @ApiOperation("查询订单详情（含订单明细）")
    public Result<OrderVO> detail(Long id) {
        log.info("查询订单详情：{}", id);
        return Result.success(ordersService.getOrderDetail(id));
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getUserId,userId);
        List<Orders> list = ordersService.list(qw);

        List<OrderVO> voList = new ArrayList<>();
        for(Orders orders: list){
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(orders,vo);
            voList.add(vo);
        }

        return Result.success(voList);
    }

    @GetMapping("/cancel")
    public Result cancel(Long id){
        LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
        uw.eq(Orders::getId,id);
        uw.eq(Orders::getStatus,1);
        uw.set(Orders::getStatus,6);
        uw.set(Orders::getCancelReason,"用户取消订单");
        uw.set(Orders::getCancelTime, LocalDateTime.now());

        boolean row = ordersService.update(uw);
        if(row==false){
            throw new OrderStatusException("只有待支付的订单可以取消");
        }
        return Result.success();
    }


}
