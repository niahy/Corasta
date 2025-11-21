package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员内容列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminContentListItemResponse {

    private Long id;
    private String type;
    private String title;
    private String summary;
    private Long authorId;
    private String authorName;
    private Integer status;
    private Integer auditStatus;
    private LocalDateTime createdAt;
}

