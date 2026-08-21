package com.example.backend.repository;

import com.example.backend.entity.Renewal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 续时记录数据访问层
 */
@Repository
public interface RenewalRepository extends JpaRepository<Renewal, Long> {

    /**
     * 根据会话ID查询续时记录
     */
    List<Renewal> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
