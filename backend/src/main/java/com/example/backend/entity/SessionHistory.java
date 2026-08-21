package com.example.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作历史实体
 */
@Data
@Entity
@Table(name = "session_history")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 入座记录ID
     */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * 操作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActionType action;

    /**
     * 操作详情（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * 操作人
     */
    @Column(length = 100)
    private String operator;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ActionType {
        check_in,   // 入座
        renew,      // 续时
        end,        // 结束
        cancel      // 取消
    }
}
