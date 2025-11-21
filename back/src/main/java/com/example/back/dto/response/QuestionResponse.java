package com.example.back.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题响应
 */
@Data
@Builder
public class QuestionResponse {

    private Long id;
    private String title;
    private LocalDateTime createdAt;
}

