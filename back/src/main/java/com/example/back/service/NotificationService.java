package com.example.back.service;

import com.example.back.dto.request.NotificationQueryRequest;
import com.example.back.dto.response.NotificationListResponse;
import com.example.back.dto.response.NotificationMarkAllResponse;
import com.example.back.dto.response.NotificationUnreadCountResponse;
import com.example.back.entity.User;

/**
 * 通知服务
 */
public interface NotificationService {

    NotificationListResponse getNotifications(NotificationQueryRequest request);

    void markAsRead(Long notificationId);

    NotificationMarkAllResponse markAllAsRead();

    NotificationUnreadCountResponse getUnreadCount();

    void sendFollowNotification(User sender, User receiver);
}

