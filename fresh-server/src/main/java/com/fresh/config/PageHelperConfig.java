package com.fresh.config;

import com.github.pagehelper.PageInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * PageHelper 分页插件配置
 * 不使用 pagehelper-spring-boot-starter（它强依赖原生 mybatis starter，
 * 与 MyBatis-Plus 冲突），改为手动注册分页拦截器，
 * MyBatis-Plus 的自动配置会把所有 Interceptor 类型的 Bean 装配进 SqlSessionFactory
 */
@Configuration
public class PageHelperConfig {

    @Bean
    public PageInterceptor pageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        Properties properties = new Properties();
        //指定数据库方言为 MySQL
        properties.setProperty("helperDialect", "mysql");
        //分页合理化：页码小于 1 查第一页，大于最大页查最后一页
        properties.setProperty("reasonable", "true");
        pageInterceptor.setProperties(properties);
        return pageInterceptor;
    }
}
