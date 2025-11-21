package com.example.back.dto.request;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 通知查询请求
 */
@Data
public class NotificationQueryRequest {

    private Integer page = 1;

    private Integer pageSize = 20;

    private String type;

    private Boolean read;

    public String normalizedType() {
        return StringUtils.hasText(type) ? type.trim().toLowerCase() : null;
    }
}

