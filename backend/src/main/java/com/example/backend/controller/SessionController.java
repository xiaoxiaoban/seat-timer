package com.example.backend.controller;

import com.example.backend.dto.SessionCreateDTO;
import com.example.backend.dto.SessionRenewDTO;
import com.example.backend.dto.SessionUpdateInfoDTO;
import com.example.backend.dto.TodayStatsDTO;
import com.example.backend.entity.Session;
import com.example.backend.entity.SessionHistory;
import com.example.backend.response.ApiResponse;
import com.example.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入座记录控制器
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * 创建入座记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createSession(@Valid @RequestBody SessionCreateDTO dto) {
        Session session = sessionService.createSession(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        return ResponseEntity.ok(ApiResponse.success(data, "入座成功"));
    }

    /**
     * 根据ID获取会话
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getSessionById(@PathVariable Long id) {
        Session session = sessionService.getSessionById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 续时
     */
    @PutMapping("/{id}/renew")
    public ResponseEntity<ApiResponse> renewSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionRenewDTO dto) {
        Session session = sessionService.renewSession(id, dto);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        data.put("newEndTime", session.getEndTime());
        return ResponseEntity.ok(ApiResponse.success(data, "续时成功"));
    }

    /**
     * 结束体验
     */
    @PutMapping("/{id}/end")
    public ResponseEntity<ApiResponse> endSession(@PathVariable Long id) {
        Session session = sessionService.endSession(id);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        return ResponseEntity.ok(ApiResponse.success(data, "体验已结束"));
    }

    /**
     * 取消会话
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelSession(@PathVariable Long id) {
        Session session = sessionService.cancelSession(id);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        return ResponseEntity.ok(ApiResponse.success(data, "会话已取消"));
    }

    /**
     * 获取会话历史记录
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse> getSessionHistory(@PathVariable Long id) {
        List<SessionHistory> history = sessionService.getSessionHistory(id);
        Map<String, Object> data = new HashMap<>();
        data.put("history", history);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 获取今日统计
     */
    @GetMapping("/stats/today")
    public ResponseEntity<ApiResponse> getTodayStats() {
        TodayStatsDTO stats = sessionService.getTodayStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 更新会话备注
     */
    @PutMapping("/{id}/note")
    public ResponseEntity<ApiResponse> updateSessionNote(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Session session = sessionService.updateSessionNote(id, request.get("note"));
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        return ResponseEntity.ok(ApiResponse.success(data, "备注更新成功"));
    }

    /**
     * 更新会话信息（备注、付款状态、核销状态）
     */
    @PutMapping("/{id}/info")
    public ResponseEntity<ApiResponse> updateSessionInfo(
            @PathVariable Long id,
            @RequestBody SessionUpdateInfoDTO dto) {
        Session session = sessionService.updateSessionInfo(id, dto);
        Map<String, Object> data = new HashMap<>();
        data.put("session", session);
        return ResponseEntity.ok(ApiResponse.success(data, "信息更新成功"));
    }
}
