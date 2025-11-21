package com.example.back.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新隐私设置请求
 */
@Data
public class UpdatePrivacySettingsRequest {

    @NotNull(message = "主页可见性不能为空")
    @Min(value = 0, message = "主页可见性最小为0")
    @Max(value = 1, message = "主页可见性最大为1")
    private Integer homepageVisible;

    @NotNull(message = "内容可见性不能为空")
    @Min(value = 0, message = "内容可见性最小为0")
    @Max(value = 1, message = "内容可见性最大为1")
    private Integer contentVisible;
}

