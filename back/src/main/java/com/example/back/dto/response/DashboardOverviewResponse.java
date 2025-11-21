package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仪表盘概览响应
 */
@Data
@Builder
@AllArgsConstructor
public class DashboardOverviewResponse {

    private Stats stats;
    private List<ContentCard> recentContents;
    private List<TodoItem> pendingItems;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Stats {
        private Long articleCount;
        private Long draftCount;
        private Long questionCount;
        private Long answerCount;
        private Long followerCount;
        private Long totalViews;
        private Long totalLikes;
        private Long totalFavorites;
        private Long totalComments;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ContentCard {
        private Long id;
        private String type;
        private String title;
        private String status;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class TodoItem {
        private String type;
        private String description;
        private Long targetId;
        private LocalDateTime createdAt;
    }
}

