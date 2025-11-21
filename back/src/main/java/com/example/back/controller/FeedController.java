package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.FeedQueryRequest;
import com.example.back.dto.response.FeedItemResponse;
import com.example.back.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动态Feed控制器
 */
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    public Result<PageResult<FeedItemResponse>> getFeed(@Valid FeedQueryRequest request) {
        return Result.success(feedService.getFeed(request));
    }
}

