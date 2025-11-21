package com.example.back.controller;

import com.example.back.common.Result;
import com.example.back.dto.request.LikeRequest;
import com.example.back.dto.response.LikeStatusResponse;
import com.example.back.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public Result<LikeStatusResponse> like(@Valid @RequestBody LikeRequest request) {
        return Result.success("点赞成功", likeService.like(request));
    }

    @DeleteMapping
    public Result<Void> cancel(@RequestParam String targetType,
                               @RequestParam Long targetId) {
        likeService.cancel(targetType, targetId);
        return Result.success("取消点赞成功", null);
    }
}

