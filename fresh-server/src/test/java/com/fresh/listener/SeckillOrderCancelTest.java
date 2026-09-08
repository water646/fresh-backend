package com.fresh.listener;

import com.fresh.SeckillTestSupport;
import com.fresh.context.BaseContext;
import com.fresh.entity.SeckillOrders;
import com.fresh.exception.BaseException;
import com.fresh.exception.OrderStatusException;
import com.fresh.service.SeckillOrdersService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 秒杀订单超时取消 + 回补库存的集成测试（投 1 秒短延时消息触发真实监听器）
 * 以及用户端取消接口（直接调 Service）
 */
public class SeckillOrderCancelTest extends SeckillTestSupport {

    private static final long TEST_USER = 990001L;

    @Autowired
    private SeckillOrdersService seckillOrdersService;

    /**
     * 用例1：待付款订单到时 → 取消订单，且数据库库存、Redis 库存、一人一单三处都回补
     */
    @Test
    public void 待付款订单超时_应取消并回补数据库与Redis库存() throws Exception {
        insertGoods(5); //数据库库存5
        SeckillOrders order = insertOrder("TC" + System.nanoTime(), 1, 0, TEST_USER);
        stringRedisTemplate.opsForValue().set(stockKey(), "7"); //Redis库存7
        stringRedisTemplate.opsForSet().add(orderKey(), String.valueOf(TEST_USER));

        sendDelayMessage(order.getNumber(), 1000);
        awaitOrderStatus(order.getNumber(), 6);

        SeckillOrders db = findByNumber(order.getNumber());
        assertEquals(6, db.getStatus().intValue());
        assertEquals("订单超时取消", db.getCancelReason());
        assertNotNull(db.getCancelTime());
        assertEquals(6, dbStock()); //数据库库存 5 + 1
        assertEquals("8", redisStock()); //Redis库存 7 + 1
        assertFalse(stringRedisTemplate.opsForSet().isMember(orderKey(), String.valueOf(TEST_USER)));
    }

    /**
     * 用例2：已支付订单到时 → 不取消、不回补（乐观锁 rows=0 门控，防止已售库存被凭空加回导致超卖）
     */
    @Test
    public void 已支付订单到时_不应取消也不应回补库存() throws Exception {
        insertGoods(5);
        SeckillOrders order = insertOrder("TC" + System.nanoTime(), 2, 1, TEST_USER);
        stringRedisTemplate.opsForValue().set(stockKey(), "7");
        stringRedisTemplate.opsForSet().add(orderKey(), String.valueOf(TEST_USER));

        sendDelayMessage(order.getNumber(), 1000);
        //已支付路径没有任何状态变化可轮询，固定等过延时点（1秒延时 + 余量）
        Thread.sleep(3500);

        SeckillOrders db = findByNumber(order.getNumber());
        assertEquals(2, db.getStatus().intValue());
        assertEquals(1, db.getPayStatus().intValue());
        assertNull(db.getCancelReason());
        assertNull(db.getCancelTime());
        assertEquals(5, dbStock()); //库存原样
        assertEquals("7", redisStock());
        assertTrue(stringRedisTemplate.opsForSet().isMember(orderKey(), String.valueOf(TEST_USER)));
    }

    /**
     * 用例3：同一条取消消息重复消费两次 → 只回补一次（幂等）
     */
    @Test
    public void 重复的取消消息_只回补一次库存() throws Exception {
        insertGoods(5);
        SeckillOrders order = insertOrder("TC" + System.nanoTime(), 1, 0, TEST_USER);
        stringRedisTemplate.opsForValue().set(stockKey(), "7");
        stringRedisTemplate.opsForSet().add(orderKey(), String.valueOf(TEST_USER));

        sendDelayMessage(order.getNumber(), 1000);
        sendDelayMessage(order.getNumber(), 1000);
        awaitOrderStatus(order.getNumber(), 6);
        //再等第二条消息消费完（它会发现订单已取消而不回补）
        Thread.sleep(2500);

        assertEquals(6, dbStock()); //只 +1
        assertEquals("8", redisStock()); //只 +1
    }

    /**
     * 用例4：用户取消自己的待付款订单 → 取消成功并三处回补（与超时取消共用同一套回补逻辑）
     */
    @Test
    public void 用户取消待付款订单_应取消并回补库存() {
        insertGoods(5);
        SeckillOrders order = insertOrder("TC" + System.nanoTime(), 1, 0, TEST_USER);
        stringRedisTemplate.opsForValue().set(stockKey(), "7");
        stringRedisTemplate.opsForSet().add(orderKey(), String.valueOf(TEST_USER));

        BaseContext.setCurrentId(TEST_USER);
        try {
            seckillOrdersService.cancel(order.getId());
        } finally {
            BaseContext.removeCurrentId();
        }

        SeckillOrders db = findByNumber(order.getNumber());
        assertEquals(6, db.getStatus().intValue());
        assertEquals("用户取消订单", db.getCancelReason());
        assertNotNull(db.getCancelTime());
        assertEquals(6, dbStock()); //数据库库存 5 + 1
        assertEquals("8", redisStock()); //Redis库存 7 + 1
        assertFalse(stringRedisTemplate.opsForSet().isMember(orderKey(), String.valueOf(TEST_USER)));
    }

    /**
     * 用例5：取消别人的订单按"订单不存在"拒绝；取消已支付订单被状态校验拒绝且库存不动
     */
    @Test
    public void 用户取消校验_越权与已支付订单都应被拒绝() {
        insertGoods(5);
        SeckillOrders othersOrder = insertOrder("TC" + System.nanoTime(), 1, 0, 990002L);
        SeckillOrders paidOrder = insertOrder("TC" + System.nanoTime(), 2, 1, TEST_USER);
        stringRedisTemplate.opsForValue().set(stockKey(), "7");
        stringRedisTemplate.opsForSet().add(orderKey(), String.valueOf(TEST_USER));

        BaseContext.setCurrentId(TEST_USER);
        try {
            //别人的订单：按不存在处理，避免越权探测
            BaseException e1 = assertThrows(BaseException.class,
                    () -> seckillOrdersService.cancel(othersOrder.getId()));
            assertEquals("订单不存在", e1.getMessage());

            //已支付订单：只有待付款的订单可以取消
            OrderStatusException e2 = assertThrows(OrderStatusException.class,
                    () -> seckillOrdersService.cancel(paidOrder.getId()));
            assertEquals("只有待支付的订单可以取消", e2.getMessage());
        } finally {
            BaseContext.removeCurrentId();
        }

        //两次拒绝都不该动任何数据
        assertEquals(5, dbStock());
        assertEquals("7", redisStock());
        assertEquals(1, findByNumber(othersOrder.getNumber()).getStatus().intValue());
        assertEquals(2, findByNumber(paidOrder.getNumber()).getStatus().intValue());
    }
}
