package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.context.AuthUser;
import com.example.back.dto.response.FollowUserItemResponse;
import com.example.back.entity.Follow;
import com.example.back.entity.User;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.FollowRepository;
import com.example.back.repository.UserRepository;
import com.example.back.service.FollowService;
import com.example.back.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 关注服务实现
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void followUser(Long userId) {
        Long currentUserId = AuthContextHolder.requireUserId();
        if (Objects.equals(currentUserId, userId)) {
            throw new ValidationException("不能关注自己");
        }
        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("当前用户不存在"));
        User following = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("目标用户不存在"));
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return;
        }
        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();
        followRepository.save(follow);
        notificationService.sendFollowNotification(follower, following);
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId) {
        Long currentUserId = AuthContextHolder.requireUserId();
        if (Objects.equals(currentUserId, userId)) {
            return;
        }
        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("当前用户不存在"));
        User following = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("目标用户不存在"));
        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresent(followRepository::delete);
    }

    @Override
    public PageResult<FollowUserItemResponse> getFollowing(Long userId, Integer page, Integer pageSize) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        Pageable pageable = buildPageable(page, pageSize);
        Page<Follow> followPage = followRepository.findByFollower(targetUser, pageable);
        return buildResult(followPage, Follow::getFollowing);
    }

    @Override
    public PageResult<FollowUserItemResponse> getFollowers(Long userId, Integer page, Integer pageSize) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        Pageable pageable = buildPageable(page, pageSize);
        Page<Follow> followPage = followRepository.findByFollowing(targetUser, pageable);
        return buildResult(followPage, Follow::getFollower);
    }

    private Pageable buildPageable(Integer page, Integer pageSize) {
        int pageIndex = Math.max(0, Optional.ofNullable(page).orElse(1) - 1);
        int size = Optional.ofNullable(pageSize).orElse(20);
        return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private PageResult<FollowUserItemResponse> buildResult(Page<Follow> followPage,
                                                           java.util.function.Function<Follow, User> userExtractor) {
        List<User> users = followPage.stream()
                .map(userExtractor)
                .collect(Collectors.toList());
        List<Long> userIds = users.stream().map(User::getId).distinct().toList();

        Map<Long, Long> followerCountMap = countFollowers(userIds);
        Map<Long, Long> followingCountMap = countFollowing(userIds);
        Set<Long> viewerFollowing = resolveViewerFollowing(userIds);

        List<FollowUserItemResponse> items = followPage.stream()
                .map(follow -> {
                    User user = userExtractor.apply(follow);
                    return FollowUserItemResponse.builder()
                            .id(user.getId())
                            .username(user.getUsername())
                            .nickname(user.getNickname())
                            .avatar(user.getAvatar())
                            .bio(user.getBio())
                            .followerCount(followerCountMap.getOrDefault(user.getId(), 0L))
                            .followingCount(followingCountMap.getOrDefault(user.getId(), 0L))
                            .following(viewerFollowing.contains(user.getId()))
                            .followedAt(follow.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResult.of(items,
                followPage.getNumber() + 1,
                followPage.getSize(),
                followPage.getTotalElements());
    }

    private Map<Long, Long> countFollowers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return followRepository.countFollowersByUserIds(userIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    private Map<Long, Long> countFollowing(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return followRepository.countFollowingByUserIds(userIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    private Set<Long> resolveViewerFollowing(List<Long> targetIds) {
        AuthUser viewer = AuthContextHolder.getCurrentUser().orElse(null);
        if (viewer == null || targetIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> ids = followRepository.findFollowingIds(viewer.getId(), targetIds);
        return new HashSet<>(ids);
    }
}

