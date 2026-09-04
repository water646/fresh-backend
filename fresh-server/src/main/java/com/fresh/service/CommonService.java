package com.fresh.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    /**
     * 文件上传：保存到本地存储目录，返回可直接访问的 url
     * @param file 前端上传的文件
     * @return 文件访问地址
     */
    String upload(MultipartFile file);
}
