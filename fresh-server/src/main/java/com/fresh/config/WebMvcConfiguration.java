package com.fresh.config;

import com.fresh.interceptor.JwtTokenAdminInterceptor;
import com.fresh.interceptor.JwtTokenUserInterceptor;
import com.fresh.json.JacksonObjectMapper;
import com.fresh.properties.FileStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springdoc.core.models.GroupedOpenApi;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        //TODO 开发管理端登录接口后，放行登录路径，例如：
        //.excludePathPatterns("/admin/employee/login")
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        //放行用户端登录相关路径（验证码、登录本身无需 token）
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/sendMsg", "/user/user/login");
    }

    /**
     * 管理端接口文档分组（原 springfox Docket 的 springdoc 等价物）
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理端接口")
                .pathsToMatch("/admin/**")
                .build();
    }

    /**
     * 用户端接口文档分组
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户端接口")
                .pathsToMatch("/user/**")
                .build();
    }

    /**
     * 接口文档全局信息（标题、版本、描述）
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Fresh Market项目接口文档")
                .version("2.0")
                .description("Fresh Market项目接口文档"));
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        //本地文件存储目录映射为 /uploads/**，上传的文件通过该路径直接访问（不走拦截器，无需 token）
        String location = fileStorageProperties.getBaseDir().replace("\\", "/");
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        log.info("注册静态资源映射：/uploads/** -> {}", location);
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + location);
    }

    //扩展spring mvc框架的消息转换器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //把自定义 ObjectMapper 设置到已有 Jackson 转换器上，而不是往队首插入新转换器：
        //springdoc 的 /v3/api-docs 返回 byte[]，若 Jackson 转换器排在 ByteArray 转换器之前，
        //byte[] 会被 Jackson 序列化成 base64 字符串，knife4j 文档页面解析失败
        converters.stream()
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(MappingJackson2HttpMessageConverter.class::cast)
                .findFirst()
                .ifPresent(converter -> converter.setObjectMapper(new JacksonObjectMapper()));
    }
}
