package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索响应
 */
@Data
@Builder
public class SearchResponse {

    private Section<ArticleItem> articles;
    private Section<QuestionItem> questions;
    private Section<UserItem> users;
    private Section<Object> videos;
    private Long total;

    @Data
    @AllArgsConstructor
    public static class Section<T> {
        private List<T> items;
        private Long total;
    }

    @Data
    @AllArgsConstructor
    public static class ArticleItem {
        private Long id;
        private String title;
        private String summary;
        private String coverImage;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private LocalDateTime createdAt;
        private SimplifiedUser author;
    }

    @Data
    @AllArgsConstructor
    public static class QuestionItem {
        private Long id;
        private String title;
        private String description;
        private Integer viewCount;
        private Integer answerCount;
        private Integer followCount;
        private LocalDateTime createdAt;
        private SimplifiedUser author;
    }

    @Data
    @AllArgsConstructor
    public static class UserItem {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String bio;
    }

    @Data
    @AllArgsConstructor
    public static class SimplifiedUser {
        private Long id;
        private String nickname;
        private String avatar;
    }
}

