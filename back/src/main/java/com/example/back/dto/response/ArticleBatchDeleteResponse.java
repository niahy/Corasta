package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文章批量删除结果
 */
@Data
@AllArgsConstructor
public class ArticleBatchDeleteResponse {

    private Integer deletedCount;
}

