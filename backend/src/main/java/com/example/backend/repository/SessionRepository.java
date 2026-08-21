package com.example.backend.repository;

import com.example.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 入座记录数据访问层
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * 根据座位ID和状态查询会话
     */
    Optional<Session> findBySeatIdAndStatus(Long seatId, Session.SessionStatus status);

    /**
     * 查询进行中的会话
     */
    List<Session> findByStatus(Session.SessionStatus status);

    /**
     * 查询座位的当前活动会话
     */
    @Query("SELECT s FROM Session s WHERE s.seatId = ?1 AND s.status = 'active'")
    Optional<Session> findActiveSessionBySeatId(Long seatId);

    /**
     * 查询今日创建的会话
     */
    @Query("SELECT s FROM Session s WHERE DATE(s.createdAt) = CURRENT_DATE")
    List<Session> findTodaySessions();

    /**
     * 查询即将到期的会话（15分钟内）
     */
    @Query("SELECT s FROM Session s WHERE s.status = 'active' AND s.endTime BETWEEN ?1 AND ?2")
    List<Session> findExpiringSessions(LocalDateTime now, LocalDateTime expireTime);

    /**
     * 根据座位ID查询所有会话（按开始时间倒序）
     */
    List<Session> findBySeatIdOrderByStartTimeDesc(Long seatId);
}
