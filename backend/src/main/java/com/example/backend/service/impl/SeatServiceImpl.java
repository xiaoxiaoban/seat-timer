package com.example.backend.service.impl;

import com.example.backend.dto.SeatCreateDTO;
import com.example.backend.dto.SeatUpdateDTO;
import com.example.backend.entity.Seat;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.SeatRepository;
import com.example.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 座位服务实现
 */
@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    public List<Seat> getAllSeats() {
        return seatRepository.findAllByOrderByIdAsc();
    }

    @Override
    public Seat getSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new BusinessException("座位不存在"));
    }

    @Override
    @Transactional
    public Seat createSeat(SeatCreateDTO dto) {
        Seat seat = Seat.builder()
                .name(dto.getName())
                .x(dto.getX())
                .y(dto.getY())
                .status(Seat.SeatStatus.idle)
                .build();
        return seatRepository.save(seat);
    }

    @Override
    @Transactional
    public Seat updateSeat(Long id, SeatUpdateDTO dto) {
        Seat seat = getSeatById(id);

        if (dto.getName() != null) {
            seat.setName(dto.getName());
        }
        if (dto.getX() != null) {
            seat.setX(dto.getX());
        }
        if (dto.getY() != null) {
            seat.setY(dto.getY());
        }

        return seatRepository.save(seat);
    }

    @Override
    @Transactional
    public void deleteSeat(Long id) {
        Seat seat = getSeatById(id);
        seatRepository.delete(seat);
    }

    @Override
    @Transactional
    public void initDefaultSeats() {
        // 如果已经有座位，不初始化
        if (seatRepository.count() > 0) {
            return;
        }

        // 创建 3x3 默认座位布局
        String[] rows = {"A", "B", "C"};
        int index = 0;

        for (int i = 0; i < rows.length; i++) {
            for (int j = 1; j <= 3; j++) {
                Seat seat = Seat.builder()
                        .name(rows[i] + j)
                        .x(60 + (j - 1) * 100)
                        .y(60 + i * 100)
                        .status(Seat.SeatStatus.idle)
                        .build();
                seatRepository.save(seat);
                index++;
            }
        }
    }
}
