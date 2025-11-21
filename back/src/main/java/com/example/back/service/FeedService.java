package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.FeedQueryRequest;
import com.example.back.dto.response.FeedItemResponse;

/**
 * Feed服务
 */
public interface FeedService {

    PageResult<FeedItemResponse> getFeed(FeedQueryRequest request);
}

