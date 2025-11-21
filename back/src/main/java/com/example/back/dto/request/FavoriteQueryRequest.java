package com.example.back.dto.request;

import lombok.Data;

/**
 * 收藏列表查询
 */
@Data
public class FavoriteQueryRequest {

    private Long folderId;

    private String targetType;

    private Integer page = 1;

    private Integer pageSize = 20;

    public String normalizedTargetType() {
        return targetType == null ? null : targetType.trim().toLowerCase();
    }
}

