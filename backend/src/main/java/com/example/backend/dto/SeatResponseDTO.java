package com.example.backend.dto;

import com.example.backend.entity.Seat;
import com.example.backend.entity.Session;
import lombok.Data;

/**
 * 座位响应DTO（包含当前会话）
 */
@Data
public class SeatResponseDTO {

    private Long id;
    private String name;
    private Integer x;
    private Integer y;
    private String status;
    private Session currentSession;

    public static SeatResponseDTO fromEntity(Seat seat, Session currentSession) {
        SeatResponseDTO dto = new SeatResponseDTO();
        dto.setId(seat.getId());
        dto.setName(seat.getName());
        dto.setX(seat.getX());
        dto.setY(seat.getY());
        dto.setStatus(seat.getStatus().name().toLowerCase());
        dto.setCurrentSession(currentSession);
        return dto;
    }
}
