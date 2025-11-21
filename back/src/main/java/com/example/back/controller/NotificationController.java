package com.example.back.controller;

import com.example.back.common.Result;
import com.example.back.dto.request.NotificationQueryRequest;
import com.example.back.dto.response.NotificationListResponse;
import com.example.back.dto.response.NotificationMarkAllResponse;
import com.example.back.dto.response.NotificationUnreadCountResponse;
import com.example.back.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Result<NotificationListResponse> list(@Valid NotificationQueryRequest request) {
        return Result.success(notificationService.getNotifications(request));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("标记成功", null);
    }

    @PutMapping("/read-all")
    public Result<NotificationMarkAllResponse> markAllRead() {
        return Result.success(notificationService.markAllAsRead());
    }

    @GetMapping("/unread-count")
    public Result<NotificationUnreadCountResponse> unreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }
}

