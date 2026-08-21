package com.example.backend.controller;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.response.ApiResponse;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@Valid @RequestBody UserCreateDTO dto) {
        log.info("创建用户: {}", dto.getUsername());
        UserResponseDTO user = userService.createUser(dto);
        return ResponseEntity.ok(ApiResponse.success(user, "用户创建成功"));
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id) {
        log.info("获取用户: {}", id);
        Optional<UserResponseDTO> user = userService.getUserById(id);
        return user.map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error(404, "用户不存在")));
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("获取用户列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDTO> users = userService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto) {
        log.info("更新用户: {}", id);
        Optional<UserResponseDTO> user = userService.updateUser(id, dto);
        return user.map(u -> ResponseEntity.ok(ApiResponse.success(u, "用户更新成功")))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error(404, "用户不存在")));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("删除用户: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "用户删除成功"));
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名获取用户: {}", username);
        Optional<UserResponseDTO> user = userService.getUserByUsername(username);
        return user.map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.error(404, "用户不存在")));
    }
}
