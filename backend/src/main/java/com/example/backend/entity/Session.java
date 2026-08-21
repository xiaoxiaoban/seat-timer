package com.example.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入座记录实体
 */
@Data
@Entity
@Table(name = "sessions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 座位ID
     */
    @Column(name = "seat_id", nullable = false)
    private Long seatId;

    /**
     * 客户姓名
     */
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    /**
     * 手机号
     */
    @Column(nullable = false, length = 20)
    private String phone;

    /**
     * 体验时长（小时）
     */
    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 预计结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 实际结束时间
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 备注
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    /**
     * 团购券信息
     */
    @Column(name = "coupon_info", length = 255)
    private String couponInfo;

    /**
     * 是否已付款
     */
    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = false;

    /**
     * 是否已核销
     */
    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    /**
     * 续时总时长（小时）
     */
    @Column(name = "renewal_hours")
    @Builder.Default
    private Integer renewalHours = 0;

    /**
     * 总金额
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /**
     * 状态：active-进行中，completed-已完成，cancelled-已取消
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.active;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SessionStatus {
        active,     // 进行中
        completed,  // 已完成
        cancelled   // 已取消
    }
}
