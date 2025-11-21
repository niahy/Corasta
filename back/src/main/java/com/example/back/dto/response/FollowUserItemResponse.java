package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 关注/粉丝列表项
 */
@Data
@Builder
@AllArgsConstructor
public class FollowUserItemResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String bio;
    private Long followerCount;
    private Long followingCount;
    private Boolean following;
    private LocalDateTime followedAt;
}

