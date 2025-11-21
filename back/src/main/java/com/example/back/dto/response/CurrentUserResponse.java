package com.example.back.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 当前用户信息响应
 */
@Data
@Builder
public class CurrentUserResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Integer followerCount;
    private Integer followingCount;
    private Integer likeCount;
    private Integer articleCount;
    private Integer questionCount;
    private Integer answerCount;
}

