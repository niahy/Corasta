package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员用户列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListItemResponse {

    private Long id;
    private String username;
    private String nickname;
    private Integer status;
    private Integer role;
    private Long articleCount;
    private Long questionCount;
    private Long answerCount;
    private Long followerCount;
    private LocalDateTime createdAt;
}

