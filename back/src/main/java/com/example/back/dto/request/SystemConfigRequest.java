package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统配置更新请求
 */
@Data
public class SystemConfigRequest {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 100, message = "站点名称长度不能超过100")
    private String siteName;

    @Size(max = 200, message = "站点描述长度不能超过200")
    private String siteDescription;

    private Boolean reviewEnabled = Boolean.TRUE;
}

