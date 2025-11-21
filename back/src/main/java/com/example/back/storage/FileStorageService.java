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
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;

    private static final String AVATAR_PREFIX = "avatar";

    public String storeAvatar(Long userId, MultipartFile file) {
        validateImage(file);
        return store(file, properties.getAvatarDir(), AVATAR_PREFIX + "_" + userId);
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

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.contains("png") || contentType.contains("jpeg") || contentType.contains("jpg"))) {
            throw new ValidationException("仅支持 PNG/JPG 格式的图片");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new ValidationException("图片大小不能超过2MB");
        }
    }
}

