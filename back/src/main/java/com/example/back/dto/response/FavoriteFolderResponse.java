package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹响应
 */
@Data
@Builder
@AllArgsConstructor
public class FavoriteFolderResponse {

    private Long id;
    private String name;
    private String description;
    private Integer itemCount;
    private LocalDateTime createdAt;
}

