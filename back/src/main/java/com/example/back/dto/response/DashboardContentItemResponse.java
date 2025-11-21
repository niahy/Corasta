package com.example.back.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容列表项
 */
@Data
@Builder
public class DashboardContentItemResponse {

    private Long id;
    private String type;
    private String title;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

