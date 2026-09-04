package com.fresh.listener;

import com.fresh.config.RabbitMqConfig;
import com.fresh.entity.SeckillOrders;
import com.fresh.service.SeckillOrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消息监听器：消费 MQ 中的秒杀订单消息并写入数据库，
 * 秒杀接口只负责扣减 Redis 库存和发消息，落库（存订单 + 扣减数据库库存）由本监听器异步完成（削峰）
 */
@Component
@Slf4j
public class SeckillOrderListener {

    @Autowired
    private SeckillOrdersService seckillOrdersService;

    /**
     * 监听秒杀订单队列，把订单写入数据库
     * @param order 秒杀接口发来的订单消息
     */
    @RabbitListener(queues = RabbitMqConfig.SECKILL_QUEUE)
    public void listenSeckillOrder(SeckillOrders order) {
        log.info("收到秒杀订单消息：{}", order.getNumber());

        //幂等处理：MQ 可能重复投递同一条消息（消费超时重试等），按订单号判重避免重复落库
        Long count = seckillOrdersService.lambdaQuery()
                .eq(SeckillOrders::getNumber, order.getNumber())
                .count();
        if (count != null && count > 0) {
            log.info("订单 {} 已落库，跳过重复消息", order.getNumber());
            return;
        }

        //同一事务内：原子扣减数据库库存（stock-1，库存不足则不落库）+ 写入订单
        seckillOrdersService.createOrder(order);
    }
}
