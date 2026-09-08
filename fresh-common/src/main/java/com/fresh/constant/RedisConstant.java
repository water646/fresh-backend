package com.fresh.constant;

/**
 * Redis相关常量
 */
public class RedisConstant {

    /**
     * 用户端登录验证码的key前缀，完整key为 login:code:手机号
     */
    public static final String LOGIN_CODE_KEY = "login:code:";

    /**
     * 登录验证码有效期（分钟）
     */
    public static final Long LOGIN_CODE_TTL = 5L;

    /**
     * 分类商品列表缓存的key前缀，完整key为 goods:分类id，value 为商品列表 JSON
     */
    public static final String GOODS_CACHE_KEY = "goods:";

    /**
     * 订单号自增序列的key前缀，完整key为 icr:orders:日期(yyyyMMdd)，
     * 普通订单与秒杀订单共用同一序列，保证单号不重复
     */
    public static final String ORDER_SEQUENCE_KEY = "icr:orders:";

    /**
     * 秒杀Redis库存的key前缀，完整key为 fresh:seckill:stock:秒杀商品id。
     * 注意：seckill.lua / seckillCancel.lua 中有同款字符串，改名需两处同步
     */
    public static final String SECKILL_STOCK_KEY = "fresh:seckill:stock:";

    /**
     * 秒杀一人一单用户集合的key前缀，完整key为 fresh:seckill:order:秒杀商品id。
     * 注意：seckill.lua / seckillCancel.lua 中有同款字符串，改名需两处同步
     */
    public static final String SECKILL_ORDER_KEY = "fresh:seckill:order:";

    /**
     * 支付接口防重复提交的 Redisson 分布式锁key前缀，完整key为 payment_lock:订单号
     */
    public static final String PAYMENT_LOCK_KEY = "payment_lock:";
}
