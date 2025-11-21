package com.example.back.dto.request;

import com.example.back.common.Constants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 回答查询请求
 */
@Data
public class AnswerQueryRequest {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = Constants.MAX_PAGE_SIZE, message = "每页数量不能超过100")
    private Integer pageSize = Constants.DEFAULT_PAGE_SIZE;

    private String sort = "votes";

    public String normalizedSort() {
        return StringUtils.hasText(sort) ? sort.trim() : "votes";
    }
}

