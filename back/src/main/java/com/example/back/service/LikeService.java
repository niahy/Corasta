package com.example.back.service;

import com.example.back.dto.request.LikeRequest;
import com.example.back.dto.response.LikeStatusResponse;

/**
 * 点赞服务
 */
public interface LikeService {

    LikeStatusResponse like(LikeRequest request);

    void cancel(String targetType, Long targetId);
}

