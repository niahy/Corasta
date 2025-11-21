package com.example.back.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 内容列表查询
 */
@Data
public class DashboardContentQueryRequest {

    @Pattern(regexp = "articles|questions|answers", message = "内容类型不合法")
    private String type = "articles";

    private String status;

    private String keyword;

    private Integer page = 1;

    private Integer pageSize = 20;

    public String normalizedType() {
        if (!StringUtils.hasText(type)) {
            return "articles";
        }
        return type.toLowerCase();
    }

    public String normalizedStatus() {
        return StringUtils.hasText(status) ? status.toLowerCase() : null;
    }
}

