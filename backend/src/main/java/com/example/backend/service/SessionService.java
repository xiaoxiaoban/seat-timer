package com.example.backend.service;

import com.example.backend.dto.*;
import com.example.backend.entity.Session;
import com.example.backend.entity.SessionHistory;

import java.util.List;

/**
 * 入座记录服务接口
 */
public interface SessionService {

    /**
     * 创建入座记录
     */
    Session createSession(SessionCreateDTO dto);

    /**
     * 根据ID获取会话
     */
    Session getSessionById(Long id);

    /**
     * 获取座位的当前活动会话
     */
    Session getActiveSessionBySeatId(Long seatId);

    /**
     * 续时
     */
    Session renewSession(Long sessionId, SessionRenewDTO dto);

    /**
     * 结束体验
     */
    Session endSession(Long sessionId);

    /**
     * 取消会话
     */
    Session cancelSession(Long sessionId);

    /**
     * 获取会话历史记录
     */
    List<SessionHistory> getSessionHistory(Long sessionId);

    /**
     * 获取座位的所有会话历史记录
     */
    List<Session> getSeatSessions(Long seatId);

    /**
     * 更新会话备注
     */
    Session updateSessionNote(Long sessionId, String note);

    /**
     * 更新会话信息（备注、付款状态、核销状态）
     */
    Session updateSessionInfo(Long sessionId, SessionUpdateInfoDTO dto);

    /**
     * 获取今日统计
     */
    TodayStatsDTO getTodayStats();
}
