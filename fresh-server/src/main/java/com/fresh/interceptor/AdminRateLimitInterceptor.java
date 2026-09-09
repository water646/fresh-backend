package com.fresh.interceptor;

import com.alibaba.fastjson.JSON;
import com.fresh.constant.RedisConstant;
import com.fresh.context.BaseContext;
import com.fresh.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;

/**
 * 管理端接口限流拦截器：同一管理端用户在时间窗口内最多调用 N 次接口（默认 60 次/分钟）。
 * 必须注册在 JwtTokenAdminInterceptor 之后，依赖其解析 JWT 后存入 BaseContext 的员工 id；
 * 计数用 Redis 固定窗口：incr + 首次设置过期（ratelimit.lua 原子执行），窗口结束 key 自动过期清零
 */
@Component
@Slf4j
public class AdminRateLimitInterceptor implements HandlerInterceptor {

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("ratelimit.lua")));
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 窗口内最大调用次数
     */
    @Value("${fresh.rate-limit.count:60}")
    private long maxCount;

    /**
     * 计数窗口（秒）
     */
    @Value("${fresh.rate-limit.window-seconds:60}")
    private long windowSeconds;

    /**
     * 计数并判断是否放行
     * @param request
     * @param response
     * @param handler
     * @return true 放行；false 已超限，响应体为 Result.error 的 JSON
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId = BaseContext.getCurrentId();
        //没经过 jwt 校验（理论上不会发生，登录路径已在注册时放行），直接放行
        if (userId == null) {
            return true;
        }

        long count;
        try {
            count = stringRedisTemplate.execute(RATE_LIMIT_SCRIPT,
                    Collections.singletonList(RedisConstant.ADMIN_RATE_LIMIT_KEY + userId),
                    String.valueOf(windowSeconds));
        } catch (Exception ex) {
            //Redis 不可用时放行：限流是防御性功能，不阻断主流程，记错误日志便于发现
            log.error("接口限流计数失败，本次放行：{}", ex.getMessage());
            return true;
        }

        if (count > maxCount) {
            log.info("管理端用户 {} 触发限流，窗口内第 {} 次调用：{}", userId, count, request.getRequestURI());
            //与全局异常处理保持一致：HTTP 200 + Result.error JSON，前端按 code 字段判断
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JSON.toJSONString(Result.error("操作过于频繁，请稍后再试")));
            return false;
        }
        return true;
    }
}
