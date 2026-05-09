package com.kkmall.account;

import com.kkmall.account.application.AccountProfileApplicationService;
import com.kkmall.account.domain.Gender;
import com.kkmall.account.domain.PhoneNumber;
import com.kkmall.account.domain.Role;
import com.kkmall.account.domain.User;
import com.kkmall.account.domain.UserRepository;
import com.kkmall.account.interfaces.dto.UserProfileDto;
import com.kkmall.account.interfaces.dto.UserProfileDtoMapper;
import com.kkmall.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountProfileApplicationServiceTest {

    private UserRepository userRepository;
    private UserProfileDtoMapper dtoMapper;
    private AccountProfileApplicationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        // 手动实现 dtoMapper 以避免依赖 MapStruct 生成的实现
        dtoMapper = user -> {
            UserProfileDto dto = new UserProfileDto();
            dto.setId(user.getId());
            dto.setPhone(user.getPhone().value());
            dto.setNickname(user.getNickname());
            dto.setAvatarUrl(user.getAvatarUrl());
            dto.setGender(user.getGender() == null ? "UNKNOWN" : user.getGender().name());
            dto.setBirthday(user.getBirthday());
            dto.setRole(user.getRole().name());
            dto.setCreatedAt(user.getCreatedAt());
            return dto;
        };
        service = new AccountProfileApplicationService(userRepository, dtoMapper);
    }

    @Test
    void updatesCurrentUserProfileWithValidatedFields() {
        User user = User.restore(1L, PhoneNumber.of("13800138000"), "会员8000", null,
                Gender.UNKNOWN, null, Role.CUSTOMER, LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AccountProfileApplicationService.ProfileRequest request = new AccountProfileApplicationService.ProfileRequest();
        request.setNickname("KK 用户");
        request.setAvatarUrl("/api/v1/files/avatars/2026-05-06/avatar.jpg");
        request.setGender("FEMALE");
        request.setBirthday(LocalDate.of(1992, 8, 12));

        UserProfileDto profile = service.updateProfile(1L, request);

        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getPhone()).isEqualTo("13800138000");
        assertThat(profile.getNickname()).isEqualTo("KK 用户");
        assertThat(profile.getAvatarUrl()).isEqualTo("/api/v1/files/avatars/2026-05-06/avatar.jpg");
        assertThat(profile.getGender()).isEqualTo("FEMALE");
        assertThat(profile.getBirthday()).isEqualTo(LocalDate.of(1992, 8, 12));
    }

    @Test
    void rejectsFutureBirthday() {
        User user = User.restore(1L, PhoneNumber.of("13800138000"), "会员8000", null,
                Gender.UNKNOWN, null, Role.CUSTOMER, LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AccountProfileApplicationService.ProfileRequest request = new AccountProfileApplicationService.ProfileRequest();
        request.setNickname("KK 用户");
        request.setGender("UNKNOWN");
        request.setBirthday(LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.updateProfile(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PROFILE_BIRTHDAY_INVALID");
    }
}
