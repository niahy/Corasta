package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.DashboardContentQueryRequest;
import com.example.back.dto.request.InteractionHistoryRequest;
import com.example.back.dto.response.DashboardContentItemResponse;
import com.example.back.dto.response.DashboardContentResponse;
import com.example.back.dto.response.DashboardOverviewResponse;
import com.example.back.dto.response.DashboardTrendResponse;
import com.example.back.dto.response.InteractionHistoryItemResponse;

/**
 * 用户内容后台服务
 */
public interface DashboardService {

    DashboardOverviewResponse getOverview();

    DashboardContentResponse getContent();

    PageResult<DashboardContentItemResponse> listContents(DashboardContentQueryRequest request);

    PageResult<InteractionHistoryItemResponse> interactionHistory(InteractionHistoryRequest request);

    DashboardTrendResponse getTrends();
}

