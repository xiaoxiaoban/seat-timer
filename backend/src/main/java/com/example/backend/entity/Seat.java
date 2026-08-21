package com.example.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 座位实体
 */
@Data
@Entity
@Table(name = "seats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 座位名称/编号
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 画布X坐标
     */
    @Column(nullable = false)
    private Integer x = 0;

    /**
     * 画布Y坐标
     */
    @Column(nullable = false)
    private Integer y = 0;

    /**
     * 状态：idle-空闲，occupied-使用中
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SeatStatus status = SeatStatus.idle;

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

    /**
     * 当前会话（非持久化字段）
     */
    @Transient
    private Session currentSession;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SeatStatus {
        idle,       // 空闲
        occupied    // 使用中
    }
}
