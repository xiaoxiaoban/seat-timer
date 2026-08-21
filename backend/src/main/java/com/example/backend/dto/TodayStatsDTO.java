package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 今日统计DTO
 */
@Data
public class TodayStatsDTO {

    private Integer totalSessions;      // 今日总入座数
    private Integer activeSessions;     // 当前进行中
    private BigDecimal totalRevenue;    // 今日收入估算
    private Double avgDuration;         // 平均时长
}
