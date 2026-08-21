package com.example.backend.controller;

import com.example.backend.dto.SeatCreateDTO;
import com.example.backend.dto.SeatResponseDTO;
import com.example.backend.dto.SeatUpdateDTO;
import com.example.backend.dto.SessionHistoryDTO;
import com.example.backend.entity.Seat;
import com.example.backend.entity.Session;
import com.example.backend.response.ApiResponse;
import com.example.backend.service.SeatService;
import com.example.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 座位控制器
 */
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final SessionService sessionService;

    /**
     * 获取所有座位（包含当前会话）
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllSeats() {
        List<Seat> seats = seatService.getAllSeats();

        // 转换为包含当前会话的DTO
        List<SeatResponseDTO> seatDTOs = seats.stream().map(seat -> {
            Session currentSession = null;
            if (seat.getStatus() == Seat.SeatStatus.occupied) {
                try {
                    currentSession = sessionService.getActiveSessionBySeatId(seat.getId());
                } catch (Exception e) {
                    // 忽略错误，可能没有活动会话
                }
            }
            return SeatResponseDTO.fromEntity(seat, currentSession);
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("seats", seatDTOs);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 根据ID获取座位
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSeatById(@PathVariable Long id) {
        Seat seat = seatService.getSeatById(id);

        // 获取当前会话
        Session currentSession = null;
        if (seat.getStatus() == Seat.SeatStatus.occupied) {
            try {
                currentSession = sessionService.getActiveSessionBySeatId(id);
            } catch (Exception e) {
                // 忽略错误
            }
        }

        SeatResponseDTO dto = SeatResponseDTO.fromEntity(seat, currentSession);
        Map<String, Object> data = new HashMap<>();
        data.put("seat", dto);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 创建座位
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createSeat(@Valid @RequestBody SeatCreateDTO dto) {
        Seat seat = seatService.createSeat(dto);
        SeatResponseDTO responseDTO = SeatResponseDTO.fromEntity(seat, null);
        Map<String, Object> data = new HashMap<>();
        data.put("seat", responseDTO);
        return ResponseEntity.ok(ApiResponse.success(data, "座位创建成功"));
    }

    /**
     * 更新座位
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateSeat(
            @PathVariable Long id,
            @RequestBody SeatUpdateDTO dto) {
        Seat seat = seatService.updateSeat(id, dto);
        SeatResponseDTO responseDTO = SeatResponseDTO.fromEntity(seat, null);
        Map<String, Object> data = new HashMap<>();
        data.put("seat", responseDTO);
        return ResponseEntity.ok(ApiResponse.success(data, "座位更新成功"));
    }

    /**
     * 删除座位
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok(ApiResponse.success(null, "座位删除成功"));
    }

    /**
     * 初始化默认座位
     */
    @PostMapping("/init")
    public ResponseEntity<ApiResponse> initSeats() {
        seatService.initDefaultSeats();
        return ResponseEntity.ok(ApiResponse.success(null, "默认座位初始化成功"));
    }

    /**
     * 获取座位的所有会话历史记录
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse> getSeatHistory(@PathVariable Long id) {
        // 获取座位信息
        Seat seat = seatService.getSeatById(id);

        // 获取座位的所有会话
        List<Session> sessions = sessionService.getSeatSessions(id);

        // 转换为DTO列表
        List<SessionHistoryDTO> historyDTOs = sessions.stream().map(session -> {
            SessionHistoryDTO dto = new SessionHistoryDTO();
            dto.setId(session.getId());
            dto.setSeatId(session.getSeatId());
            dto.setSeatName(seat.getName());  // 设置座位名称
            dto.setCustomerName(session.getCustomerName());
            dto.setPhone(session.getPhone());
            dto.setDurationHours(session.getDurationHours());
            dto.setRenewalHours(session.getRenewalHours());
            dto.setStartTime(session.getStartTime());
            dto.setEndTime(session.getEndTime());
            dto.setActualEndTime(session.getActualEndTime());
            dto.setStatus(session.getStatus().name());
            dto.setNote(session.getNote());
            dto.setCouponInfo(session.getCouponInfo());
            dto.setIsPaid(session.getIsPaid());
            dto.setIsVerified(session.getIsVerified());
            dto.setTotalAmount(session.getTotalAmount());
            dto.setCreatedAt(session.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("history", historyDTOs);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
