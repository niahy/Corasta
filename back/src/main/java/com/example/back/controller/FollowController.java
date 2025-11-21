package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.response.FollowUserItemResponse;
import com.example.back.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public Result<Void> follow(@PathVariable Long userId) {
        followService.followUser(userId);
        return Result.success("关注成功", null);
    }

    @DeleteMapping("/{userId}/follow")
    public Result<Void> unfollow(@PathVariable Long userId) {
        followService.unfollowUser(userId);
        return Result.success("取消关注成功", null);
    }

    @GetMapping("/{userId}/following")
    public Result<PageResult<FollowUserItemResponse>> following(@PathVariable Long userId,
                                                                @RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(followService.getFollowing(userId, page, pageSize));
    }

    @GetMapping("/{userId}/followers")
    public Result<PageResult<FollowUserItemResponse>> followers(@PathVariable Long userId,
                                                                @RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(followService.getFollowers(userId, page, pageSize));
    }
}

