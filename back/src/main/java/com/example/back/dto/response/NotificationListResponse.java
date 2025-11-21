package com.example.back.dto.response;

import com.example.back.common.PageResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 通知列表响应
 */
@Data
@Builder
@AllArgsConstructor
public class NotificationListResponse {

    private List<NotificationResponse> items;
    private PageResult.Pagination pagination;
    private Long unreadCount;
}

