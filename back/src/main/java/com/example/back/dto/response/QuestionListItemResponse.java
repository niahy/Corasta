package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题列表项
 */
@Data
@Builder
public class QuestionListItemResponse {

    private Long id;
    private String title;
    private String description;
    private Integer answerCount;
    private Integer followCount;
    private LocalDateTime createdAt;
    private AuthorInfo author;
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
    public static class TagInfo {
        private Long id;
        private String name;
    }
}

