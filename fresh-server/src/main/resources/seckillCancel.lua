-- 原子回补秒杀库存并移除一人一单记录（订单超时取消时调用）
local goodsId = ARGV[1]
local userId = ARGV[2]

--库存key（与 RedisConstant.SECKILL_STOCK_KEY 保持一致，改名需两处同步）
local stockKey = 'fresh:seckill:stock:'..goodsId

--订单key（与 RedisConstant.SECKILL_ORDER_KEY 保持一致，改名需两处同步）
local orderKey = 'fresh:seckill:order:'..goodsId

--回补库存：key不存在说明缓存未预热或已被清理，此时不凭空创建库存（等管理端预热时按数据库值重建）
if(redis.call('exists',stockKey)==1) then
    redis.call('incrby',stockKey,1)
end

--移除一人一单记录，订单取消后允许该用户重新秒杀
redis.call('srem',orderKey,userId)

return 0
