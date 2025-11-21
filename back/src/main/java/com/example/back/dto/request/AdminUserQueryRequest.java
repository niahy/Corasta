package com.example.back.dto.request;

import lombok.Data;

/**
 * 管理员用户查询请求
 */
@Data
public class AdminUserQueryRequest {

    private String keyword;

    private Integer status;

    private Integer page = 1;

    private Integer pageSize = 20;
}

