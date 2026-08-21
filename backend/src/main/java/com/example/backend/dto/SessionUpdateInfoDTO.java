package com.example.backend.dto;

import lombok.Data;

/**
 * 更新会话信息请求DTO
 */
@Data
public class SessionUpdateInfoDTO {

    private String note;
    private Boolean isPaid;
    private Boolean isVerified;
}
