package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.Seat;

import java.util.List;

/**
 * 座位服务接口
 */
public interface SeatService {

    /**
     * 获取所有座位
     */
    List<Seat> getAllSeats();

    /**
     * 根据ID获取座位
     */
    Seat getSeatById(Long id);

    /**
     * 创建座位
     */
    Seat createSeat(SeatCreateDTO dto);

    /**
     * 更新座位
     */
    Seat updateSeat(Long id, SeatUpdateDTO dto);

    /**
     * 删除座位
     */
    void deleteSeat(Long id);

    /**
     * 初始化默认座位
     */
    void initDefaultSeats();
}
