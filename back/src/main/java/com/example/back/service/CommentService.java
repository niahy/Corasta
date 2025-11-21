package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.CommentCreateRequest;
import com.example.back.dto.request.CommentQueryRequest;
import com.example.back.dto.request.CommentUpdateRequest;
import com.example.back.dto.response.CommentLikeResponse;
import com.example.back.dto.response.CommentListItemResponse;
import com.example.back.dto.response.CommentResponse;

/**
 * 评论服务
 */
public interface CommentService {

    CommentResponse createComment(CommentCreateRequest request);

    PageResult<CommentListItemResponse> getComments(CommentQueryRequest request);

    CommentResponse updateComment(Long commentId, CommentUpdateRequest request);

    void deleteComment(Long commentId);

    CommentLikeResponse likeComment(Long commentId);

    void unlikeComment(Long commentId);

    void pinComment(Long commentId);
}

