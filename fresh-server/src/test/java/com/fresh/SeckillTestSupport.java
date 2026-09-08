package com.fresh;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.constant.RedisConstant;
import com.fresh.entity.SeckillGoods;
import com.fresh.entity.SeckillOrders;
import com.fresh.mapper.SeckillMapper;
import com.fresh.mapper.SeckillOrdersMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * 秒杀测试公共基类：连本机真实中间件（MySQL/Redis/RabbitMQ）的集成测试。
 * 数据策略：每个用例自造数据（商品/订单/Redis key），结束后只清理自己造的部分。
 * 注意：不能用 @Transactional 回滚——MQ 消息和 Redis 写入不会随测试事务回滚。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SeckillTestSupport {

    /**
     * 延时消息交换机（与 OrderDelayListener 声明的一致），
     * 测试里把延时从 15 分钟缩到 1 秒，触发的是同一个监听器
     */
    protected static final String DELAY_EXCHANGE = "fresh.order.delay.direct";
    protected static final String DELAY_ROUTING_KEY = "delay";

    @Autowired
    protected SeckillMapper seckillMapper;
    @Autowired
    protected SeckillOrdersMapper seckillOrdersMapper;
    @Autowired
    protected StringRedisTemplate stringRedisTemplate;
    @Autowired
    protected RabbitTemplate rabbitTemplate;

    protected SeckillGoods goods;

    /**
     * 只清理本测试自己造的数据：按商品 id 删订单（含 MQ 异步落库的）、删商品、删两个 Redis key
     */
    @AfterEach
    void cleanUp() {
        if (goods != null && goods.getId() != null) {
            seckillOrdersMapper.delete(new LambdaQueryWrapper<SeckillOrders>()
                    .eq(SeckillOrders::getSeckillGoodsId, goods.getId()));
            seckillMapper.deleteById(goods.getId());
            stringRedisTemplate.delete(Arrays.asList(stockKey(), orderKey()));
        }
    }

    /**
     * 造一个启用中、时间窗口覆盖当前的秒杀商品（name 是 varchar(20)，注意别超长）
     */
    protected SeckillGoods insertGoods(int dbStock) {
        SeckillGoods g = new SeckillGoods();
        g.setName("t" + System.nanoTime() % 100000000);
        g.setSeckillPrice(new BigDecimal("1.00"));
        g.setStock(dbStock);
        g.setLimitNum(1);
        g.setStartTime(LocalDateTime.now().minusMinutes(5));
        g.setEndTime(LocalDateTime.now().plusMinutes(5));
        g.setStatus(1);
        seckillMapper.insert(g);
        this.goods = g;
        return g;
    }

    /**
     * 造一笔秒杀订单（payStatus 为已支付时补齐支付相关字段）
     */
    protected SeckillOrders insertOrder(String number, int status, int payStatus, long userId) {
        SeckillOrders o = new SeckillOrders();
        o.setNumber(number);
        o.setSeckillGoodsId(goods.getId());
        o.setStatus(status);
        o.setUserId(userId);
        o.setPayStatus(payStatus);
        o.setOrderTime(LocalDateTime.now());
        o.setAmount(new BigDecimal("1.00"));
        if (payStatus == SeckillOrders.PAY_STATUS_PAID) {
            o.setPayMethod(1);
            o.setCheckoutTime(LocalDateTime.now());
        }
        seckillOrdersMapper.insert(o);
        return o;
    }

    /**
     * 投一条短延时消息，走生产同款的延迟交换机和消息转换器（Jackson 字符串）
     */
    protected void sendDelayMessage(String number, int delayMillis) {
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, DELAY_ROUTING_KEY, number, msg -> {
            //spring-amqp 3.x 移除了 setDelay(int)，改用 setDelayLong
            msg.getMessageProperties().setDelayLong((long) delayMillis);
            return msg;
        });
    }

    /**
     * 轮询等待订单状态变化（延时消息有秒级延迟，不能用固定 sleep 等正向路径）
     */
    protected void awaitOrderStatus(String number, int expectedStatus) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            SeckillOrders o = findByNumber(number);
            if (o != null && o.getStatus() == expectedStatus) {
                return;
            }
            Thread.sleep(200);
        }
        fail("等待订单 " + number + " 状态变为 " + expectedStatus + " 超时");
    }

    /**
     * 轮询等待该商品的订单数达到预期（秒杀下单是 MQ 异步落库的）
     */
    protected void awaitOrderCount(int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            if (orderCount() == expected) {
                return;
            }
            Thread.sleep(200);
        }
        fail("等待订单数达到 " + expected + " 超时，当前 " + orderCount());
    }

    protected SeckillOrders findByNumber(String number) {
        return seckillOrdersMapper.selectOne(new LambdaQueryWrapper<SeckillOrders>()
                .eq(SeckillOrders::getNumber, number));
    }

    protected int orderCount() {
        return seckillOrdersMapper.selectCount(new LambdaQueryWrapper<SeckillOrders>()
                .eq(SeckillOrders::getSeckillGoodsId, goods.getId())).intValue();
    }

    protected int dbStock() {
        return seckillMapper.selectById(goods.getId()).getStock();
    }

    protected String redisStock() {
        return stringRedisTemplate.opsForValue().get(stockKey());
    }

    protected String stockKey() {
        return RedisConstant.SECKILL_STOCK_KEY + goods.getId();
    }

    protected String orderKey() {
        return RedisConstant.SECKILL_ORDER_KEY + goods.getId();
    }
}
