-- 原子判断库存是否充足并扣减，以及用户是否已经下过单
local goodsId = ARGV[1]
local userId = ARGV[2]

--库存key
local stockKey = 'fresh:seckill:stock:'..goodsId

--订单key
local orderKey = 'fresh:seckill:order:'..goodsId

-- 判断库存是否充足（key不存在时get返回nil，视为库存不足，避免lua直接报错）
local stock = tonumber(redis.call('get',stockKey))
if (stock == nil or stock<=0) then
    return 1
end

--确保一人一单,用SISMEMBER判断用户是否存在于集合
if(redis.call('sismember',orderKey,userId)==1) then
    return 2
end

--扣减库存,记录用户下单
redis.call('incrby',stockKey,-1)
redis.call('sadd',orderKey,userId)

return 0




