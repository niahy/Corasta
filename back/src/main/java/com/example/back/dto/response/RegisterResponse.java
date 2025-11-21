package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 注册响应
 */
@Data
@AllArgsConstructor
public class RegisterResponse {

    private Long userId;

    private String username;
}

