package com.example.backend.service.impl;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.*;
import com.example.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入座记录服务实现
 */
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final RenewalRepository renewalRepository;
    private final SessionHistoryRepository historyRepository;

    // 每小时收费标准
    private static final BigDecimal HOURLY_RATE = new BigDecimal("50.00");

    @Override
    @Transactional
    public Session createSession(SessionCreateDTO dto) {
        // 检查座位是否存在
        Seat seat = seatRepository.findById(dto.getSeatId())
                .orElseThrow(() -> new BusinessException("座位不存在"));

        // 检查座位是否已被占用
        if (seat.getStatus() == Seat.SeatStatus.occupied) {
            throw new BusinessException("该座位已被占用");
        }

        // 检查是否已有活动会话
        sessionRepository.findActiveSessionBySeatId(dto.getSeatId())
                .ifPresent(s -> { throw new BusinessException("该座位已有进行中的会话"); });

        // 计算结束时间
        LocalDateTime endTime = dto.getStartTime().plusHours(dto.getDurationHours());

        // 创建会话
        Session session = Session.builder()
                .seatId(dto.getSeatId())
                .customerName(dto.getCustomerName())
                .phone(dto.getPhone())
                .durationHours(dto.getDurationHours())
                .startTime(dto.getStartTime())
                .endTime(endTime)
                .note(dto.getNote())
                .couponInfo(dto.getCouponInfo())
                .isPaid(dto.getIsPaid())
                .isVerified(dto.getIsVerified())
                .renewalHours(0)
                .totalAmount(HOURLY_RATE.multiply(new BigDecimal(dto.getDurationHours())))
                .status(Session.SessionStatus.active)
                .build();

        session = sessionRepository.save(session);

        // 更新座位状态
        seat.setStatus(Seat.SeatStatus.occupied);
        seatRepository.save(seat);

        // 记录历史
        recordHistory(session.getId(), SessionHistory.ActionType.check_in,
                String.format("客户:%s, 时长:%d小时", dto.getCustomerName(), dto.getDurationHours()));

        return session;
    }

    @Override
    public Session getSessionById(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("会话不存在"));
    }

    @Override
    public Session getActiveSessionBySeatId(Long seatId) {
        return sessionRepository.findActiveSessionBySeatId(seatId)
                .orElse(null);
    }

    @Override
    @Transactional
    public Session renewSession(Long sessionId, SessionRenewDTO dto) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (session.getStatus() != Session.SessionStatus.active) {
            throw new BusinessException("会话已结束，无法续时");
        }

        LocalDateTime previousEndTime = session.getEndTime();
        LocalDateTime newEndTime = previousEndTime.plusHours(dto.getRenewalHours());

        // 创建续时记录
        Renewal renewal = Renewal.builder()
                .sessionId(sessionId)
                .renewalHours(dto.getRenewalHours())
                .previousEndTime(previousEndTime)
                .newEndTime(newEndTime)
                .build();
        renewalRepository.save(renewal);

        // 更新会话
        session.setEndTime(newEndTime);
        session.setRenewalHours(session.getRenewalHours() + dto.getRenewalHours());
        session.setTotalAmount(session.getTotalAmount().add(
                HOURLY_RATE.multiply(new BigDecimal(dto.getRenewalHours()))));
        session = sessionRepository.save(session);

        // 记录历史
        recordHistory(sessionId, SessionHistory.ActionType.renew,
                String.format("续时:%d小时, 新结束时间:%s", dto.getRenewalHours(), newEndTime));

        return session;
    }

    @Override
    @Transactional
    public Session endSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (session.getStatus() != Session.SessionStatus.active) {
            throw new BusinessException("会话已结束");
        }

        LocalDateTime actualEndTime = LocalDateTime.now();

        // 更新会话
        session.setActualEndTime(actualEndTime);
        session.setStatus(Session.SessionStatus.completed);
        session = sessionRepository.save(session);

        // 更新座位状态
        Seat seat = seatRepository.findById(session.getSeatId())
                .orElseThrow(() -> new BusinessException("座位不存在"));
        seat.setStatus(Seat.SeatStatus.idle);
        seatRepository.save(seat);

        // 记录历史
        recordHistory(sessionId, SessionHistory.ActionType.end,
                String.format("实际结束时间:%s", actualEndTime));

        return session;
    }

    @Override
    @Transactional
    public Session cancelSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        if (session.getStatus() != Session.SessionStatus.active) {
            throw new BusinessException("会话已结束，无法取消");
        }

        // 更新会话
        session.setStatus(Session.SessionStatus.cancelled);
        session = sessionRepository.save(session);

        // 更新座位状态
        Seat seat = seatRepository.findById(session.getSeatId())
                .orElseThrow(() -> new BusinessException("座位不存在"));
        seat.setStatus(Seat.SeatStatus.idle);
        seatRepository.save(seat);

        // 记录历史
        recordHistory(sessionId, SessionHistory.ActionType.cancel,
                String.format("取消时间:%s", LocalDateTime.now()));

        return session;
    }

    @Override
    public List<SessionHistory> getSessionHistory(Long sessionId) {
        // 验证会话存在
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        return historyRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }

    @Override
    public List<Session> getSeatSessions(Long seatId) {
        // 验证座位存在
        seatRepository.findById(seatId)
                .orElseThrow(() -> new BusinessException("座位不存在"));

        return sessionRepository.findBySeatIdOrderByStartTimeDesc(seatId);
    }

    @Override
    @Transactional
    public Session updateSessionNote(Long sessionId, String note) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        session.setNote(note);
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public Session updateSessionInfo(Long sessionId, SessionUpdateInfoDTO dto) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在"));

        // 更新备注
        if (dto.getNote() != null) {
            session.setNote(dto.getNote());
        }
        // 更新付款状态
        if (dto.getIsPaid() != null) {
            session.setIsPaid(dto.getIsPaid());
        }
        // 更新核销状态
        if (dto.getIsVerified() != null) {
            session.setIsVerified(dto.getIsVerified());
        }

        return sessionRepository.save(session);
    }

    @Override
    public TodayStatsDTO getTodayStats() {
        List<Session> todaySessions = sessionRepository.findTodaySessions();

        int totalSessions = todaySessions.size();
        int activeSessions = (int) todaySessions.stream()
                .filter(s -> s.getStatus() == Session.SessionStatus.active)
                .count();

        BigDecimal totalRevenue = todaySessions.stream()
                .filter(s -> s.getStatus() != Session.SessionStatus.cancelled)
                .map(s -> {
                    int totalHours = s.getDurationHours() + s.getRenewalHours();
                    return HOURLY_RATE.multiply(new BigDecimal(totalHours));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgDuration = todaySessions.stream()
                .filter(s -> s.getStatus() != Session.SessionStatus.cancelled)
                .mapToInt(s -> s.getDurationHours() + s.getRenewalHours())
                .average()
                .orElse(0.0);

        TodayStatsDTO stats = new TodayStatsDTO();
        stats.setTotalSessions(totalSessions);
        stats.setActiveSessions(activeSessions);
        stats.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        stats.setAvgDuration(Math.round(avgDuration * 10.0) / 10.0);

        return stats;
    }

    /**
     * 记录操作历史
     */
    private void recordHistory(Long sessionId, SessionHistory.ActionType action, String details) {
        SessionHistory history = SessionHistory.builder()
                .sessionId(sessionId)
                .action(action)
                .details(details)
                .build();
        historyRepository.save(history);
    }
}
