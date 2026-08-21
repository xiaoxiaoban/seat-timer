package com.example.backend.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 续时请求DTO
 */
@Data
public class SessionRenewDTO {

    @NotNull(message = "续时小时数不能为空")
    @Min(value = 1, message = "续时小时数至少为1")
    private Integer renewalHours;
}
