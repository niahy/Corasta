package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情响应
 */
@Data
@Builder
public class ArticleDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String slug;
    private Integer status;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private boolean liked;
    private boolean favorited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private AuthorInfo author;
    private CategoryInfo category;
    private List<TagInfo> tags;

    @Data
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }

    @Data
    @AllArgsConstructor
    public static class CategoryInfo {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class TagInfo {
        private Long id;
        private String name;
    }
}

