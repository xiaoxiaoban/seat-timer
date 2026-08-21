package com.example.backend.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建座位请求DTO
 */
@Data
public class SeatCreateDTO {

    @NotBlank(message = "座位名称不能为空")
    private String name;

    @NotNull(message = "X坐标不能为空")
    private Integer x;

    @NotNull(message = "Y坐标不能为空")
    private Integer y;
}
