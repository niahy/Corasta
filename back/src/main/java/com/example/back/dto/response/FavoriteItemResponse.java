package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏列表项响应
 */
@Data
@Builder
public class FavoriteItemResponse {

    private Long id;
    private String targetType;
    private TargetInfo target;
    private FolderInfo folder;
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    public static class TargetInfo {
        private Long id;
        private String title;
        private String authorName;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    public static class FolderInfo {
        private Long id;
        private String name;
    }
}

