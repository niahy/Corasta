package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 问题创建/更新请求
 */
@Data
public class QuestionRequest {

    @NotBlank(message = "问题标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100字符")
    private String title;

    @NotBlank(message = "问题描述不能为空")
    @Size(max = 5000, message = "问题描述不能超过5000字符")
    private String description;

    @Size(max = 5, message = "标签最多5个")
    private List<@NotBlank(message = "标签不能为空")
            @Size(max = 50, message = "标签长度不能超过50字符") String> tags;

    private Long relatedArticleId;
}

