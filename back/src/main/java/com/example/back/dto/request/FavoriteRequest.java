package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏请求
 */
@Data
public class FavoriteRequest {

    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    private Long folderId;

    public String normalizedTargetType() {
        return targetType == null ? null : targetType.trim().toLowerCase();
    }
}

