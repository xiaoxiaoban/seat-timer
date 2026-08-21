package com.example.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 续时记录实体
 */
@Data
@Entity
@Table(name = "renewals")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renewal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 入座记录ID
     */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * 续时小时数
     */
    @Column(name = "renewal_hours", nullable = false)
    private Integer renewalHours;

    /**
     * 续时前结束时间
     */
    @Column(name = "previous_end_time", nullable = false)
    private LocalDateTime previousEndTime;

    /**
     * 续时后结束时间
     */
    @Column(name = "new_end_time", nullable = false)
    private LocalDateTime newEndTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
