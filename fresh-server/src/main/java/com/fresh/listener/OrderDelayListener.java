package com.fresh.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fresh.entity.Orders;
import com.fresh.entity.SeckillOrders;
import com.fresh.mapper.OrdersMapper;
import com.fresh.mapper.SeckillOrdersMapper;
import com.fresh.service.SeckillOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class OrderDelayListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private SeckillOrdersMapper seckillOrdersMapper;
    @Autowired
    private SeckillOrdersService seckillOrdersService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "fresh.order.delay.queue",durable = "true"),
            exchange = @Exchange(value = "fresh.order.delay.direct",delayed = "true"),
            key="delay"
    ))
    public void orderDelayListener(String orderNumber){

        LambdaQueryWrapper<Orders> qw_o = new LambdaQueryWrapper<>();
        qw_o.eq(Orders::getNumber,orderNumber);

        Orders orders = ordersMapper.selectOne(qw_o);
        if(orders!=null){
            if(Orders.PENDING_PAYMENT.equals(orders.getStatus())){
                //乐观锁防止与支付相撞
                LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
                uw.eq(Orders::getNumber,orderNumber);
                uw.eq(Orders::getStatus, Orders.PENDING_PAYMENT);
                uw.set(Orders::getStatus, Orders.CANCELLED);
                uw.set(Orders::getCancelReason,"订单超时取消");
                uw.set(Orders::getCancelTime,LocalDateTime.now());

                ordersMapper.update(null,uw);
            }
            return;
        }

        LambdaQueryWrapper<SeckillOrders> qw_s = new LambdaQueryWrapper<>();
        qw_s.eq(SeckillOrders::getNumber,orderNumber);

        SeckillOrders seckillOrders = seckillOrdersMapper.selectOne(qw_s);
        if(seckillOrders!=null){
            //取消并回补库存（取消+回补在一个事务里）：
            //乐观锁保证只有待付款订单会被取消，已支付/重复消费时不会误回补
            seckillOrdersService.cancelAndRestore(seckillOrders,"订单超时取消");
            return;
        }

        log.info(orderNumber+"订单超时取消");
    }
}
