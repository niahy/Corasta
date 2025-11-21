package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题详情响应
 */
@Data
@Builder
public class QuestionDetailResponse {

    private Long id;
    private String title;
    private String description;
    private Integer viewCount;
    private Integer answerCount;
    private Integer followCount;
    private Long bestAnswerId;
    private Boolean following;
    private LocalDateTime createdAt;
    private AuthorInfo author;
    private RelatedArticleInfo relatedArticle;
    private List<TagInfo> tags;

    @Data
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }

    @Data
    @AllArgsConstructor
    public static class RelatedArticleInfo {
        private Long id;
        private String title;
    }

    @Data
    @AllArgsConstructor
    public static class TagInfo {
        private Long id;
        private String name;
    }
}

