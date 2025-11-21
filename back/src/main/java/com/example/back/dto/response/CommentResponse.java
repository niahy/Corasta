package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论响应
 */
@Data
@Builder
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
}

