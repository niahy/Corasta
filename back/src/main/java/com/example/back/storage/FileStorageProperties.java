package com.example.back.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置
 */
@Data
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    /**
     * 上传根目录
     */
    private String uploadDir = "uploads";

    /**
     * 头像目录（相对 uploadDir）
     */
    private String avatarDir = "avatars";

    /**
     * 文章图片目录（相对 uploadDir）
     */
    private String articleImageDir = "articles";
}

