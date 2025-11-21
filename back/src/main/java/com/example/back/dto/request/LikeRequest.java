package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞请求
 */
@Data
public class LikeRequest {

    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    public String normalizedTargetType() {
        return targetType == null ? null : targetType.trim().toLowerCase();
    }
}

