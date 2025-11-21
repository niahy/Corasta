package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 点赞状态响应
 */
@Data
@AllArgsConstructor
public class LikeStatusResponse {

    private Integer likeCount;
    private Boolean liked;
}

