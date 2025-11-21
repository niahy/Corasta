package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.CommentCreateRequest;
import com.example.back.dto.request.CommentQueryRequest;
import com.example.back.dto.request.CommentUpdateRequest;
import com.example.back.dto.response.CommentLikeResponse;
import com.example.back.dto.response.CommentListItemResponse;
import com.example.back.dto.response.CommentResponse;
import com.example.back.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<CommentResponse> create(@Valid @RequestBody CommentCreateRequest request) {
        return Result.created("评论成功", commentService.createComment(request));
    }

    @GetMapping
    public Result<PageResult<CommentListItemResponse>> list(@Valid CommentQueryRequest request) {
        return Result.success(commentService.getComments(request));
    }

    @PutMapping("/{id}")
    public Result<CommentResponse> update(@PathVariable Long id,
                                          @Valid @RequestBody CommentUpdateRequest request) {
        return Result.success("更新成功", commentService.updateComment(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/like")
    public Result<CommentLikeResponse> like(@PathVariable Long id) {
        return Result.success("点赞成功", commentService.likeComment(id));
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        commentService.unlikeComment(id);
        return Result.success("取消点赞成功", null);
    }

    @PutMapping("/{id}/pin")
    public Result<Void> pin(@PathVariable Long id) {
        commentService.pinComment(id);
        return Result.success("置顶成功", null);
    }
}

