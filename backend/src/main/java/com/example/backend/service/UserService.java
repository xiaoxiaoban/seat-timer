package com.example.backend.service;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.dto.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserResponseDTO createUser(UserCreateDTO dto);

    /**
     * 根据ID查询用户
     */
    Optional<UserResponseDTO> getUserById(Long id);

    /**
     * 分页查询用户
     */
    Page<UserResponseDTO> getUsers(Pageable pageable);

    /**
     * 更新用户
     */
    Optional<UserResponseDTO> updateUser(Long id, UserUpdateDTO dto);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 根据用户名查询用户
     */
    Optional<UserResponseDTO> getUserByUsername(String username);
}
