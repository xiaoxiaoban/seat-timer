package com.example.backend.repository;

import com.example.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 座位数据访问层
 */
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    /**
     * 根据状态查询座位
     */
    List<Seat> findByStatus(Seat.SeatStatus status);

    /**
     * 查询所有座位，按ID排序
     */
    List<Seat> findAllByOrderByIdAsc();

    /**
     * 根据名称查询座位
     */
    Optional<Seat> findByName(String name);
}
