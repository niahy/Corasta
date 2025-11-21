package com.example.back.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员内容查询请求
 */
@Data
public class AdminContentQueryRequest {

    @Pattern(regexp = "articles|questions|answers", message = "内容类型不合法")
    private String type = "articles";

    private Integer page = 1;

    private Integer pageSize = 20;
}

