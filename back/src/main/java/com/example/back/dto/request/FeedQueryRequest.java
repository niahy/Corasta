package com.example.back.dto.request;

import lombok.Data;

/**
 * Feed流查询请求
 */
@Data
public class FeedQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String type = "all";

    public String normalizedType() {
        if (type == null) {
            return "all";
        }
        String normalized = type.trim().toLowerCase();
        return switch (normalized) {
            case "articles", "questions", "videos" -> normalized;
            default -> "all";
        };
    }
}

