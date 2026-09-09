-- 管理端接口限流：固定窗口计数
-- incr 计数，首次调用时设置窗口过期时间，两步必须原子执行（分开写会存在无 TTL 的 key）
local count = redis.call('incr', KEYS[1])
if count == 1 then
    redis.call('expire', KEYS[1], ARGV[1])
end
return count
