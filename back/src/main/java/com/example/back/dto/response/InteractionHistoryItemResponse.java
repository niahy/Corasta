package com.example.back.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 互动记录项
 */
@Data
@Builder
public class InteractionHistoryItemResponse {

    private String type;
    private LocalDateTime actionTime;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String targetExcerpt;
    private Long targetOwnerId;
    private String targetOwnerName;
    private Integer likeCount;
    private Integer commentCount;
}

