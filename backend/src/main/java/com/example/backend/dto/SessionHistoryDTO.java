package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会话历史DTO
 */
@Data
public class SessionHistoryDTO {

    private Long id;
    private Long seatId;
    private String seatName;  // 座位名称
    private String customerName;
    private String phone;
    private Integer durationHours;
    private Integer renewalHours;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime actualEndTime;
    private String status;
    private String note;
    private String couponInfo;
    private Boolean isPaid;
    private Boolean isVerified;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
