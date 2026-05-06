package com.kkmall.account;

import com.kkmall.account.application.AccountProfileApplicationService;
import com.kkmall.account.infrastructure.UserMapper;
import com.kkmall.account.infrastructure.UserPo;
import com.kkmall.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountProfileApplicationServiceTest {
    @Test
    void updatesCurrentUserProfileWithValidatedFields() {
        UserMapper userMapper = mock(UserMapper.class);
        UserPo user = user(1L);
        when(userMapper.selectById(1L)).thenReturn(user);
        AccountProfileApplicationService service = new AccountProfileApplicationService(userMapper);

        AccountProfileApplicationService.ProfileRequest request = new AccountProfileApplicationService.ProfileRequest();
        request.nickname = "KK 用户";
        request.avatarUrl = "/api/v1/files/avatars/2026-05-06/avatar.jpg";
        request.gender = "FEMALE";
        request.birthday = LocalDate.of(1992, 8, 12);

        Map<String, Object> profile = service.updateProfile(1L, request);

        assertThat(profile)
                .containsEntry("id", 1L)
                .containsEntry("phone", "13800138000")
                .containsEntry("nickname", "KK 用户")
                .containsEntry("avatarUrl", "/api/v1/files/avatars/2026-05-06/avatar.jpg")
                .containsEntry("gender", "FEMALE")
                .containsEntry("birthday", LocalDate.of(1992, 8, 12));
    }

    @Test
    void rejectsFutureBirthday() {
        UserMapper userMapper = mock(UserMapper.class);
        when(userMapper.selectById(1L)).thenReturn(user(1L));
        AccountProfileApplicationService service = new AccountProfileApplicationService(userMapper);

        AccountProfileApplicationService.ProfileRequest request = new AccountProfileApplicationService.ProfileRequest();
        request.nickname = "KK 用户";
        request.gender = "UNKNOWN";
        request.birthday = LocalDate.now().plusDays(1);

        assertThatThrownBy(() -> service.updateProfile(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("PROFILE_BIRTHDAY_INVALID");
    }

    private UserPo user(Long id) {
        UserPo user = new UserPo();
        user.id = id;
        user.phone = "13800138000";
        user.nickname = "会员8000";
        user.role = "CUSTOMER";
        user.gender = "UNKNOWN";
        return user;
    }
}
