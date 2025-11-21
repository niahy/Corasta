package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 未读通知数量响应
 */
@Data
@AllArgsConstructor
public class NotificationUnreadCountResponse {

    private Long count;
}

