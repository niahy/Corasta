package com.example.back.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 回答响应
 */
@Data
@Builder
public class AnswerResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
}

