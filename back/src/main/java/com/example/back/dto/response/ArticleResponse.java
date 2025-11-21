package com.example.back.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章响应（创建/更新）
 */
@Data
@Builder
public class ArticleResponse {

    private Long id;
    private String title;
    private String slug;
    private Integer status;
    private LocalDateTime createdAt;
}

