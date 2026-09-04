package com.fresh.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件存储配置
 */
@Component
@ConfigurationProperties(prefix = "fresh.file")
@Data
public class FileStorageProperties {

    /**
     * 上传文件的本地存储根目录，静态资源映射 /uploads/** 指向这里
     */
    private String baseDir;

}
