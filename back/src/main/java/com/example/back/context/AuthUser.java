package com.example.back.context;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 认证用户上下文
 */
@Data
@AllArgsConstructor
public class AuthUser {

    private Long id;
    private String username;
}

