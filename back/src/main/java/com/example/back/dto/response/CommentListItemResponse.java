package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论列表响应
 */
@Data
@Builder
public class CommentListItemResponse {

    private Long id;
    private String content;
    private Long parentId;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean liked;
    private Boolean pinned;
    private LocalDateTime createdAt;
    private AuthorInfo author;
    private List<CommentListItemResponse> replies;

    @Data
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }
}

