package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * 搜索请求
 */
@Data
public class SearchRequest {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    private String type = "all";

    private Long categoryId;

    private Long tagId;

    private Long authorId;

    private String sort = "relevance";

    private Integer page = 1;

    private Integer pageSize = 20;

    public String normalizedKeyword() {
        return keyword == null ? null : keyword.trim();
    }

    public String normalizedType() {
        if (!StringUtils.hasText(type)) {
            return "all";
        }
        return switch (type.trim().toLowerCase()) {
            case "articles", "questions", "videos", "users" -> type.trim().toLowerCase();
            default -> "all";
        };
    }

    public String normalizedSort() {
        if (!StringUtils.hasText(sort)) {
            return "relevance";
        }
        return switch (sort.trim().toLowerCase()) {
            case "latest" -> "latest";
            case "popular" -> "popular";
            default -> "relevance";
        };
    }
}

