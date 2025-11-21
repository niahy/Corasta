package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 评论点赞响应
 */
@Data
@AllArgsConstructor
public class CommentLikeResponse {

    private Integer likeCount;
    private Boolean liked;
}

