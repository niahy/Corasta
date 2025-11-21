package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知响应
 */
@Data
@Builder
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String content;
    private String targetType;
    private Long targetId;
    private FromUser fromUser;
    private Boolean read;
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    public static class FromUser {
        private Long id;
        private String nickname;
        private String avatar;
    }
}

