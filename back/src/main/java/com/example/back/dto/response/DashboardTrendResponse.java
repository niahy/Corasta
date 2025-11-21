package com.example.back.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 仪表盘趋势响应
 */
@Data
@Builder
@AllArgsConstructor
public class DashboardTrendResponse {

    private List<TrendPoint> contentTrend;
    private List<TrendPoint> followerTrend;

    @Data
    @Builder
    @AllArgsConstructor
    public static class TrendPoint {
        private LocalDate date;
        private Long value;
    }
}

