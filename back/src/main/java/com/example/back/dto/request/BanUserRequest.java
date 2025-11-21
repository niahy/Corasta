package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 封禁用户请求
 */
@Data
public class BanUserRequest {

    @NotBlank(message = "封禁原因不能为空")
    private String reason;
}

