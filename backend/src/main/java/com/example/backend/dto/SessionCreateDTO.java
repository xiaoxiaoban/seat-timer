package com.example.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 创建入座记录请求DTO
 */
@Data
public class SessionCreateDTO {

    @NotNull(message = "座位ID不能为空")
    private Long seatId;

    // 客户姓名和手机改为可选
    private String customerName;
    private String phone;

    @NotNull(message = "体验时长不能为空")
    private Integer durationHours;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    private String note;
    private String couponInfo;
    private Boolean isPaid = false;
    private Boolean isVerified = false;
}
