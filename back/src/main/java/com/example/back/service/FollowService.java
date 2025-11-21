package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.response.FollowUserItemResponse;

/**
 * 关注服务
 */
public interface FollowService {

    void followUser(Long userId);

    void unfollowUser(Long userId);

    PageResult<FollowUserItemResponse> getFollowing(Long userId, Integer page, Integer pageSize);

    PageResult<FollowUserItemResponse> getFollowers(Long userId, Integer page, Integer pageSize);
}

