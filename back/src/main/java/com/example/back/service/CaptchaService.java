package com.example.back.service;

import com.example.back.dto.response.CaptchaResponse;

/**
 * 验证码服务
 */
public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 验证码响应（包含图片和key）
     */
    CaptchaResponse generateCaptcha();

    /**
     * 验证验证码
     *
     * @param key    验证码Key
     * @param answer 用户输入的验证码
     * @return 验证是否通过
     */
    boolean verifyCaptcha(String key, String answer);
}

