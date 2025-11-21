package com.example.back.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 回答创建/更新请求
 */
@Data
public class AnswerRequest {

    @NotBlank(message = "回答内容不能为空")
    @Size(max = 10000, message = "回答内容不能超过10000字符")
    private String content;
}

