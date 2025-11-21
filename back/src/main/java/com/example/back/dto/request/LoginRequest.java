package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在8-32之间")
    private String password;

    private String captcha;

    private String captchaKey;

    private Boolean rememberMe = Boolean.FALSE;
}

