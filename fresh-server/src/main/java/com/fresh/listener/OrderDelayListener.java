package com.fresh.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fresh.entity.Orders;
import com.fresh.entity.SeckillGoods;
import com.fresh.entity.SeckillOrders;
import com.fresh.mapper.OrdersMapper;
import com.fresh.mapper.SeckillMapper;
import com.fresh.mapper.SeckillOrdersMapper;
import com.fresh.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

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
    private SeckillMapper seckillMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_CANCEL_SCRIPT;
    //在static代码块加载回补库存的lua脚本，返回值是Long
    static {
        SECKILL_CANCEL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_CANCEL_SCRIPT.setLocation(new ClassPathResource("seckillCancel.lua"));
        SECKILL_CANCEL_SCRIPT.setResultType(Long.class);
    }

    @Transactional
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
            if(orders.getStatus()==1){
                //乐观锁防止与支付相撞
                LambdaUpdateWrapper<Orders> uw = new LambdaUpdateWrapper<>();
                uw.eq(Orders::getNumber,orderNumber);
                uw.eq(Orders::getStatus,1);
                uw.set(Orders::getStatus,6);
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
            //取消订单：乐观锁保证只有待付款订单才能被取消，防止与支付相撞
            if(seckillOrders.getStatus()==1){
                LambdaUpdateWrapper<SeckillOrders> uw = new LambdaUpdateWrapper<>();
                uw.eq(SeckillOrders::getNumber,seckillOrders.getNumber());
                uw.eq(SeckillOrders::getStatus,1);
                uw.set(SeckillOrders::getCancelTime,LocalDateTime.now());
                uw.set(SeckillOrders::getCancelReason,"订单超时取消");
                uw.set(SeckillOrders::getStatus,6);

                //rows>0 说明本次真正把订单从待付款改成已取消；已支付/重复消费时rows=0，不回补库存
                int rows = seckillOrdersMapper.update(null,uw);

                if(rows>0){
                    Long goodsId = seckillOrders.getSeckillGoodsId();

                    //回补数据库库存
                    LambdaUpdateWrapper<SeckillGoods> uw_goods = new LambdaUpdateWrapper<>();
                    uw_goods.eq(SeckillGoods::getId,goodsId);
                    uw_goods.setSql("stock = stock + 1");
                    seckillMapper.update(null,uw_goods);

                    //回补Redis库存并解除一人一单（lua原子执行，避免与管理端预热缓存互相覆盖）
                    stringRedisTemplate.execute(SECKILL_CANCEL_SCRIPT, Collections.emptyList(),
                            goodsId.toString(), seckillOrders.getUserId().toString());

                    log.info("秒杀订单 {} 超时未支付，已取消并回补商品 {} 库存", orderNumber, goodsId);
                }
            }
            return;
        }

        log.info(orderNumber+"订单超时取消");
    }
}
