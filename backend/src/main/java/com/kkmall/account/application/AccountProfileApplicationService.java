package com.kkmall.account.application;

import com.kkmall.account.infrastructure.UserMapper;
import com.kkmall.account.infrastructure.UserPo;
import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Service
public class AccountProfileApplicationService {
    private static final Set<String> GENDERS = Set.of("UNKNOWN", "MALE", "FEMALE");
    private final UserMapper userMapper;

    public AccountProfileApplicationService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Map<String, Object> profile(Long userId) {
        return view(user(userId));
    }

    @Transactional
    public Map<String, Object> updateProfile(Long userId, ProfileRequest request) {
        UserPo user = user(userId);
        String nickname = request.nickname == null ? "" : request.nickname.trim();
        if (nickname.length() < 2 || nickname.length() > 32) throw new BusinessException("PROFILE_NICKNAME_INVALID");
        String gender = request.gender == null || request.gender.trim().isEmpty() ? "UNKNOWN" : request.gender.trim();
        if (!GENDERS.contains(gender)) throw new BusinessException("PROFILE_GENDER_INVALID");
        if (request.birthday != null && request.birthday.isAfter(LocalDate.now())) {
            throw new BusinessException("PROFILE_BIRTHDAY_INVALID");
        }
        user.nickname = nickname;
        user.avatarUrl = blankToNull(request.avatarUrl);
        user.gender = gender;
        user.birthday = request.birthday;
        userMapper.updateById(user);
        return view(user);
    }

    private UserPo user(Long userId) {
        UserPo user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("USER_NOT_FOUND");
        if (user.gender == null || user.gender.trim().isEmpty()) user.gender = "UNKNOWN";
        return user;
    }

    private Map<String, Object> view(UserPo user) {
        return CatalogApplicationService.mapOf(
                "id", user.id,
                "phone", user.phone,
                "nickname", user.nickname,
                "avatarUrl", user.avatarUrl,
                "gender", user.gender == null ? "UNKNOWN" : user.gender,
                "birthday", user.birthday,
                "role", user.role,
                "createdAt", user.createdAt
        );
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }

    public static class ProfileRequest {
        public String nickname;
        public String avatarUrl;
        public String gender;
        public LocalDate birthday;
    }
}
