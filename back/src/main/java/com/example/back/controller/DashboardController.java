package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.DashboardContentQueryRequest;
import com.example.back.dto.request.InteractionHistoryRequest;
import com.example.back.dto.response.DashboardContentItemResponse;
import com.example.back.dto.response.DashboardOverviewResponse;
import com.example.back.dto.response.DashboardTrendResponse;
import com.example.back.dto.response.InteractionHistoryItemResponse;
import com.example.back.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户内容后台控制器
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewResponse> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/contents")
    public Result<PageResult<DashboardContentItemResponse>> contents(@Valid DashboardContentQueryRequest request) {
        return Result.success(dashboardService.listContents(request));
    }

    @GetMapping("/interactions")
    public Result<PageResult<InteractionHistoryItemResponse>> interactions(@Valid InteractionHistoryRequest request) {
        return Result.success(dashboardService.interactionHistory(request));
    }

    @GetMapping("/trends")
    public Result<DashboardTrendResponse> trends() {
        return Result.success(dashboardService.getTrends());
    }
}

