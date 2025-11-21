package com.example.back.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 互动记录查询
 */
@Data
public class InteractionHistoryRequest {

    @Pattern(regexp = "likes|comments|favorites|answers", message = "记录类型不合法")
    private String type = "likes";

    private Integer page = 1;

    private Integer pageSize = 20;

    public String normalizedType() {
        if (!StringUtils.hasText(type)) {
            return "likes";
        }
        return type.toLowerCase();
    }
}

