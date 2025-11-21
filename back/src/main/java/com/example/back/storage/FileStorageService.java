package com.example.back.storage;

import com.example.back.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;

    private static final String AVATAR_PREFIX = "avatar";
    private static final String ARTICLE_IMAGE_PREFIX = "article";
    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024L;
    private static final long ARTICLE_IMAGE_MAX_SIZE = 5 * 1024 * 1024L;

    public String storeAvatar(Long userId, MultipartFile file) {
        validateImage(file, AVATAR_MAX_SIZE, "png", "jpeg", "jpg");
        return store(file, properties.getAvatarDir(), AVATAR_PREFIX + "_" + userId);
    }

    public String storeArticleImage(Long userId, MultipartFile file) {
        validateImage(file, ARTICLE_IMAGE_MAX_SIZE, "png", "jpeg", "jpg", "gif");
        return store(file, properties.getArticleImageDir(), ARTICLE_IMAGE_PREFIX + "_" + userId);
    }

    private String store(MultipartFile file, String dir, String prefix) {
        try {
            Path rootDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(rootDir);

            Path targetDir = rootDir.resolve(dir).normalize();
            Files.createDirectories(targetDir);

            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = prefix + "_" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                    + "_" + UUID.randomUUID().toString().replace("-", "");
            if (StringUtils.hasText(extension)) {
                filename = filename + "." + extension;
            }

            Path target = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Path relative = rootDir.relativize(target);
            return "/files/" + relative.toString().replace("\\", "/");
        } catch (IOException e) {
            throw new ValidationException("文件上传失败，请稍后再试");
        }
    }

    private void validateImage(MultipartFile file, long maxSize, String... allowedContentTypes) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("请选择要上传的文件");
        }
        String contentType = file.getContentType();
        boolean allowed = contentType != null && Arrays.stream(allowedContentTypes)
                .anyMatch(type -> contentType.toLowerCase().contains(type));
        if (!allowed) {
            throw new ValidationException("不支持的文件格式");
        }
        if (file.getSize() > maxSize) {
            throw new ValidationException("文件大小超过限制");
        }
    }
}

