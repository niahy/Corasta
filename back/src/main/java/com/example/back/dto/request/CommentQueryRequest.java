package com.example.back.dto.request;

import lombok.Data;

/**
 * 评论列表查询
 */
@Data
public class CommentQueryRequest {

    private String targetType;

    private Long targetId;

    private Integer page = 1;

    private Integer pageSize = 20;

    private String sort = "latest";

    public String normalizedTargetType() {
        return targetType == null ? null : targetType.trim().toLowerCase();
    }

    public String normalizedSort() {
        if (sort == null) {
            return "latest";
        }
        return switch (sort.trim().toLowerCase()) {
            case "oldest" -> "oldest";
            case "hot" -> "hot";
            default -> "latest";
        };
    }
}

