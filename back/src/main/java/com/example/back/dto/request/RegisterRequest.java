package com.example.back.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 用户注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度需在4-20之间")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名仅支持字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 32, message = "密码长度需在8-32之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "验证码不能为空")
    private String captcha;

    @NotBlank(message = "验证码Key不能为空")
    private String captchaKey;

    @Size(max = 3, message = "最多支持3个密保问题")
    @Valid
    private List<SecurityQuestionRequest> securityQuestions;

    @Data
    public static class SecurityQuestionRequest {

        @NotBlank(message = "问题内容不能为空")
        @Size(max = 200, message = "问题长度不能超过200字符")
        private String question;

        @NotBlank(message = "答案不能为空")
        @Size(max = 255, message = "答案长度不能超过255字符")
        private String answer;

        @NotNull(message = "排序不能为空")
        @Min(value = 1, message = "排序最小为1")
        @Max(value = 3, message = "排序最大为3")
        private Integer sortOrder;
    }
}

