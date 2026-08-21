package com.example.backend.repository;

import com.example.backend.entity.SessionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作历史数据访问层
 */
@Repository
public interface SessionHistoryRepository extends JpaRepository<SessionHistory, Long> {

    /**
     * 根据会话ID查询历史记录
     */
    List<SessionHistory> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
