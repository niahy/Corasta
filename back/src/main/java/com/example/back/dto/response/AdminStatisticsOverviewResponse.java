package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 全站统计概览
 */
@Data
@Builder
@AllArgsConstructor
public class AdminStatisticsOverviewResponse {

    private Long totalUsers;
    private Long totalArticles;
    private Long totalQuestions;
    private Long totalAnswers;
    private Long totalVideos;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;
}

