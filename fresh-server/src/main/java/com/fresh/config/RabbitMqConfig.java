package com.fresh.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置：消息转换器 + 秒杀订单的交换机/队列/绑定
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 秒杀订单交换机（direct 类型）
     */
    public static final String SECKILL_EXCHANGE = "order.direct";

    /**
     * 秒杀订单队列
     */
    public static final String SECKILL_QUEUE = "direct.queue";

    /**
     * 交换机与队列绑定的路由键
     */
    public static final String SECKILL_ROUTING_KEY = "direct";

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 消息转换器：复用 Spring 容器的 ObjectMapper（已注册 JavaTimeModule），
     * 否则消息体里的 LocalDateTime 序列化会直接抛异常
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * 声明秒杀订单的 direct 交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    /**
     * 声明秒杀订单队列（默认持久化）
     */
    @Bean
    public Queue directQueue() {
        return new Queue(SECKILL_QUEUE);
    }

    /**
     * 将队列绑定到交换机，路由键为 direct
     */
    @Bean
    public Binding directBinding() {
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(SECKILL_ROUTING_KEY);
    }

}
