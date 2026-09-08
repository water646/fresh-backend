package com.fresh.service;

import com.fresh.SeckillTestSupport;
import com.fresh.context.BaseContext;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 秒杀下单并发测试：验证 lua 脚本的原子性（不超卖、一人一单）
 */
public class SeckillConcurrencyTest extends SeckillTestSupport {

    @org.springframework.beans.factory.annotation.Autowired
    private SeckillService seckillService;

    /**
     * 用例4：20 个用户并发抢 5 件库存 → 恰好 5 人成功，Redis 与数据库都不超卖
     */
    @Test
    public void 并发抢购库存不足_不超卖() throws Exception {
        insertGoods(5); //数据库库存5（落库时逐单扣减）
        stringRedisTemplate.opsForValue().set(stockKey(), "5"); //Redis闸门5（lua判定的库存）

        int threads = 20;
        //每个线程一个不同的用户 id（990100、990101、…）
        AtomicInteger successCount = runConcurrentSeckill(threads, 990100L, 1);

        assertEquals(5, successCount.get()); //恰好5人成功，第6个人开始提示库存不足
        assertEquals("0", redisStock()); //Redis库存扣光且不为负

        //订单经 MQ 异步落库，等消费者写完后再断言数据库侧
        awaitOrderCount(5);
        assertEquals(0, dbStock()); //数据库库存 5 - 5 = 0，不超卖
    }

    /**
     * 用例5：同一用户并发发起 10 次秒杀 → 只成功 1 单，其余被一人一单拦截
     */
    @Test
    public void 同一用户并发重复抢购_只能成功一单() throws Exception {
        insertGoods(10);
        stringRedisTemplate.opsForValue().set(stockKey(), "10");

        int threads = 10;
        //所有线程用同一个用户 id（idStep=0，测的就是一人一单）
        AtomicInteger successCount = runConcurrentSeckill(threads, 990200L, 0);

        assertEquals(1, successCount.get()); //只成功1单
        assertEquals("9", redisStock()); //只扣了1件
        assertTrue(stringRedisTemplate.opsForSet().isMember(orderKey(), String.valueOf(990200L)));

        awaitOrderCount(1);
        assertEquals(9, dbStock());
    }

    /**
     * 起 N 个线程同时调 seckill()（各线程设置自己的 BaseContext 用户 id），
     * 所有线程在起跑线就绪后同时开抢，返回成功（code=1）的次数。
     * idStep=1 表示每个线程用不同用户（测超卖）；idStep=0 表示全部同一用户（测一人一单）
     */
    private AtomicInteger runConcurrentSeckill(int threads, long baseUserId, long idStep) throws Exception {
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final long uid = baseUserId + i * idStep;
            new Thread(() -> {
                BaseContext.setCurrentId(uid);
                ready.countDown();
                try {
                    go.await();
                    //seckill() 返回 Map（code 1成功 0失败），失败时 msg 为提示文案
                    Map<String, Object> result = seckillService.seckill(goods.getId());
                    if (Integer.valueOf(1).equals(result.get("code"))) {
                        successCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    BaseContext.removeCurrentId();
                    done.countDown();
                }
            }).start();
        }

        ready.await();
        go.countDown();
        done.await();
        return successCount;
    }
}
