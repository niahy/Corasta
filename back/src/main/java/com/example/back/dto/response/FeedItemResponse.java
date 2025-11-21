package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Feed流响应
 */
@Data
@Builder
public class FeedItemResponse {

    private String type;
    private Content content;
    private AuthorInfo author;
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    public static class Content {
        private Long id;
        private String title;
        private String summary;
        private String coverImage;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private Integer answerCount;
    }

    @Data
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}

