package com.fresh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 管理端 AI 对话（function calling）相关配置
 */
@Component
@ConfigurationProperties(prefix = "fresh.ai")
@Data
public class AiProperties {

    /**
     * Anthropic API 密钥（部署时替换占位符，不要提交真实密钥）
     */
    private String apiKey;

    /**
     * API 地址，留空直连官方，走代理/中转服务时填写
     */
    private String baseUrl;

    /**
     * 模型名
     */
    private String model;

}
