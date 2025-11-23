package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容管理仪表盘响应
 */
@Data
@Builder
@AllArgsConstructor
public class DashboardContentResponse {

    private Statistics statistics;
    private List<RecentItem> recentArticles;
    private List<RecentItem> recentQuestions;
    private List<RecentItem> recentAnswers;

    @Data
    @Builder
    @AllArgsConstructor
    public static class Statistics {
        private Long articleCount;
        private Long questionCount;
        private Long answerCount;
        private Long totalViewCount;
        private Long totalLikeCount;
        private Long totalCommentCount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RecentItem {
        private Long id;
        private String title;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private LocalDateTime createdAt;
    }
}

