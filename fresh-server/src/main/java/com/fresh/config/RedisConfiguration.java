package com.fresh.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始创建redis模板对象");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis的序列化器
        redisTemplate.setStringSerializer(new StringRedisSerializer());

        return redisTemplate;
    }

    /**redis 连接参数：与 application.yml 中 spring.data.redis.* 同源于 fresh.redis.*，
     * 真实密码在仓库外私密配置（~/.fresh-market/application-secret.yml），不再硬编码*/
    @Value("${fresh.redis.host}")
    private String redisHost;

    @Value("${fresh.redis.port}")
    private int redisPort;

    @Value("${fresh.redis.password}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        //添加redis地址
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setPassword(redisPassword);

        return Redisson.create(config);
    }
}
