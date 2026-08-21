package com.example.backend.dto;

import lombok.Data;

/**
 * 更新座位请求DTO
 */
@Data
public class SeatUpdateDTO {

    private String name;
    private Integer x;
    private Integer y;
}
