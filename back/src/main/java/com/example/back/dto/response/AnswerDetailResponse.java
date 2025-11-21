package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回答详情列表项
 */
@Data
@Builder
public class AnswerDetailResponse {

    private Long id;
    private String content;
    private Integer upvoteCount;
    private Integer downvoteCount;
    private Integer commentCount;
    private Boolean best;
    private Boolean upvoted;
    private Boolean downvoted;
    private LocalDateTime createdAt;
    private AuthorInfo author;

    @Data
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}

