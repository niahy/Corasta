package com.example.back.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 文章批量删除请求
 */
@Data
public class ArticleBatchDeleteRequest {

    @NotEmpty(message = "请选择要删除的文章")
    @Size(max = 50, message = "一次最多删除50篇文章")
    private List<Long> ids;
}

