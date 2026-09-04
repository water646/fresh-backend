package com.fresh.service.impl;

import com.fresh.constant.MessageConstant;
import com.fresh.exception.BaseException;
import com.fresh.properties.FileStorageProperties;
import com.fresh.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 通用服务实现：本地文件存储
 */
@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    /**
     * 允许上传的文件后缀（白名单），需要放开其他类型时在此补充
     */
    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp"));

    /**
     * 按日期分子目录，如 2026/08/28
     */
    private static final DateTimeFormatter DATE_DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Autowired
    private FileStorageProperties fileStorageProperties;

    /**
     * 文件上传：保存到本地磁盘，返回可直接访问的 url
     * @param file 前端上传的文件
     * @return 文件访问地址，如 http://localhost:8080/uploads/2026/08/28/xxx.png
     */
    public String upload(MultipartFile file) {
        //1.校验文件非空
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }

        //2.校验文件后缀（白名单），并用 UUID 重命名，避免中文/重名文件互相覆盖
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BaseException("文件类型不允许，仅支持图片：" + ALLOWED_EXTENSIONS);
        }
        String objectName = LocalDate.now().format(DATE_DIR_FORMATTER)
                + "/" + UUID.randomUUID().toString().replace("-", "")
                + "." + extension;

        //3.保存到本地存储目录
        Path targetPath = Paths.get(fileStorageProperties.getBaseDir(), objectName);
        try {
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath);
        } catch (IOException e) {
            log.error("文件保存失败：{}", e.getMessage());
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }

        //4.拼接访问 url；/uploads/** 由 WebMvcConfiguration 映射到存储目录，
        //  host:port 取自当前请求，前端拿到的地址可直接访问
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(objectName)
                .toUriString();
        log.info("文件上传成功：{}", url);
        return url;
    }

    /**
     * 提取文件后缀（小写），没有后缀返回空串
     * @param filename 原始文件名
     * @return 后缀，如 png
     */
    private String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex < 0 ? "" : filename.substring(dotIndex + 1).toLowerCase();
    }
}
