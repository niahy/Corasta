package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 标记全部已读响应
 */
@Data
@AllArgsConstructor
public class NotificationMarkAllResponse {

    private Long markedCount;
}

