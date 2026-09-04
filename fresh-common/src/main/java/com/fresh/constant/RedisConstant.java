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
}
