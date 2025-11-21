package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 用户主页响应
 */
@Data
@Builder
public class UserHomeResponse {

    private UserInfo user;
    private Boolean following;
    private ContentOverview content;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String bio;
        private Integer followerCount;
        private Integer followingCount;
        private Integer likeCount;
    }

    @Data
    @AllArgsConstructor
    public static class ContentOverview {
        private Integer articleCount;
        private Integer questionCount;
        private Integer answerCount;
    }
}

