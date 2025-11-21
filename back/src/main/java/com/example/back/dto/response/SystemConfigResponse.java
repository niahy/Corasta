package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {

    private String siteName;
    private String siteDescription;
    private Boolean reviewEnabled;
}

