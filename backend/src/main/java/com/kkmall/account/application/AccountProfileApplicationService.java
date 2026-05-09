package com.kkmall.account.application;

import com.kkmall.account.domain.User;
import com.kkmall.account.domain.UserRepository;
import com.kkmall.account.interfaces.dto.UserProfileDto;
import com.kkmall.account.interfaces.dto.UserProfileDtoMapper;
import com.kkmall.common.exception.BusinessException;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 用户资料应用服务。
 * 编排领域对象完成业务操作，不直接操作 PO。
 */
@Service
public class AccountProfileApplicationService {

    private final UserRepository userRepository;
    private final UserProfileDtoMapper dtoMapper;

    public AccountProfileApplicationService(UserRepository userRepository, UserProfileDtoMapper dtoMapper) {
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    public UserProfileDto profile(Long userId) {
        User user = getUser(userId);
        return dtoMapper.toDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, ProfileRequest request) {
        User user = getUser(userId);
        // 业务规则校验在领域对象内部完成
        user.updateProfile(
                request.getNickname(),
                request.getAvatarUrl(),
                request.getGender(),
                request.getBirthday()
        );
        userRepository.save(user);
        return dtoMapper.toDto(user);
    }

    /**
     * 将 User 领域对象转为 DTO（供 AuthApplicationService 复用）。
     */
    public UserProfileDto toDto(User user) {
        return dtoMapper.toDto(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND"));
    }

    @Data
    public static class ProfileRequest {
        private String nickname;
        private String avatarUrl;
        private String gender;
        private LocalDate birthday;
    }
}
