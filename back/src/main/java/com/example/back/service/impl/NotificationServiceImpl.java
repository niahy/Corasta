package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.NotificationQueryRequest;
import com.example.back.dto.response.NotificationListResponse;
import com.example.back.dto.response.NotificationMarkAllResponse;
import com.example.back.dto.response.NotificationResponse;
import com.example.back.dto.response.NotificationUnreadCountResponse;
import com.example.back.entity.Notification;
import com.example.back.entity.User;
import com.example.back.exception.ForbiddenException;
import com.example.back.exception.NotFoundException;
import com.example.back.repository.NotificationRepository;
import com.example.back.repository.UserRepository;
import com.example.back.service.NotificationService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public NotificationListResponse getNotifications(NotificationQueryRequest request) {
        User user = currentUser();
        Pageable pageable = PageRequest.of(
                Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1),
                Optional.ofNullable(request.getPageSize()).orElse(20),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Specification<Notification> specification = buildSpecification(user, request);
        Page<Notification> page = notificationRepository.findAll(specification, pageable);
        List<NotificationResponse> items = page.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        long unreadCount = notificationRepository.countByUserAndReadIsFalse(user);
        PageResult.Pagination pagination = new PageResult.Pagination(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return NotificationListResponse.builder()
                .items(items)
                .pagination(pagination)
                .unreadCount(unreadCount)
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        User user = currentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("通知不存在"));
        if (!Objects.equals(notification.getUser().getId(), user.getId())) {
            throw new ForbiddenException("无权操作该通知");
        }
        notification.markRead();
    }

    @Override
    @Transactional
    public NotificationMarkAllResponse markAllAsRead() {
        User user = currentUser();
        List<Notification> unread = notificationRepository.findByUserAndReadIsFalse(user);
        unread.forEach(Notification::markRead);
        return new NotificationMarkAllResponse((long) unread.size());
    }

    @Override
    public NotificationUnreadCountResponse getUnreadCount() {
        User user = currentUser();
        long count = notificationRepository.countByUserAndReadIsFalse(user);
        return new NotificationUnreadCountResponse(count);
    }

    @Override
    @Transactional
    public void sendFollowNotification(User sender, User receiver) {
        if (sender == null || receiver == null || Objects.equals(sender.getId(), receiver.getId())) {
            return;
        }
        Notification notification = Notification.builder()
                .user(receiver)
                .sender(sender)
                .type("follow")
                .title("新的关注者")
                .content(formatFollowContent(sender))
                .targetType("user")
                .targetId(sender.getId())
                .build();
        notificationRepository.save(notification);
    }

    private User currentUser() {
        Long userId = AuthContextHolder.requireUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
    }

    private Specification<Notification> buildSpecification(User user, NotificationQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));

            if (StringUtils.hasText(request.getType())) {
                predicates.add(cb.equal(root.get("type"), request.normalizedType()));
            }
            if (request.getRead() != null) {
                predicates.add(cb.equal(root.get("read"), request.getRead()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse.FromUser fromUser = null;
        if (notification.getSender() != null) {
            fromUser = new NotificationResponse.FromUser(
                    notification.getSender().getId(),
                    notification.getSender().getNickname(),
                    notification.getSender().getAvatar()
            );
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .fromUser(fromUser)
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private String formatFollowContent(User sender) {
        String displayName = StringUtils.hasText(sender.getNickname()) ? sender.getNickname() : sender.getUsername();
        return displayName + " 关注了你";
    }
}

