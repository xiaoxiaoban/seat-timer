package com.example.backend.service.impl;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.dto.UserResponseDTO;
import com.example.backend.dto.UserUpdateDTO;
import com.example.backend.entity.User;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final long CACHE_TTL = 30;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户实体
        User user = User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword()) // 实际应用应该加密
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .realName(dto.getRealName())
                .status(1)
                .build();

        user = userRepository.save(user);
        log.info("用户创建成功: {}", user.getUsername());

        // 保存到缓存
        String cacheKey = USER_CACHE_PREFIX + user.getId();
        redisTemplate.opsForValue().set(cacheKey, user, CACHE_TTL, TimeUnit.MINUTES);

        return convertToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        // 先尝试从缓存获取
        String cacheKey = USER_CACHE_PREFIX + id;
        Object cachedUser = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUser instanceof User) {
            log.debug("从缓存获取用户: {}", id);
            return Optional.of(convertToDTO((User) cachedUser));
        }

        // 从数据库查询
        return userRepository.findById(id)
                .map(user -> {
                    // 存入缓存
                    redisTemplate.opsForValue().set(cacheKey, user, CACHE_TTL, TimeUnit.MINUTES);
                    return convertToDTO(user);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public Optional<UserResponseDTO> updateUser(Long id, UserUpdateDTO dto) {
        return userRepository.findById(id)
                .map(user -> {
                    // 更新字段
                    if (dto.getUsername() != null) {
                        user.setUsername(dto.getUsername());
                    }
                    if (dto.getEmail() != null) {
                        user.setEmail(dto.getEmail());
                    }
                    if (dto.getPhone() != null) {
                        user.setPhone(dto.getPhone());
                    }
                    if (dto.getRealName() != null) {
                        user.setRealName(dto.getRealName());
                    }
                    if (dto.getStatus() != null) {
                        user.setStatus(dto.getStatus());
                    }

                    user = userRepository.save(user);
                    log.info("用户更新成功: {}", user.getUsername());

                    // 更新缓存
                    String cacheKey = USER_CACHE_PREFIX + id;
                    redisTemplate.opsForValue().set(cacheKey, user, CACHE_TTL, TimeUnit.MINUTES);

                    return convertToDTO(user);
                });
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // 删除缓存
        String cacheKey = USER_CACHE_PREFIX + id;
        redisTemplate.delete(cacheKey);

        userRepository.deleteById(id);
        log.info("用户删除成功: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    /**
     * 将实体转换为DTO
     */
    private UserResponseDTO convertToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .realName(user.getRealName())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
