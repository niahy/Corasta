package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 验证码响应
 */
@Data
@AllArgsConstructor
public class CaptchaResponse {
    /**
     * 验证码图片（Base64编码）
     */
    private String image;

    /**
     * 验证码Key（用于提交时验证）
     */
    private String key;
}

