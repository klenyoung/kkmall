package com.kkmall.account.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户聚合根。
 * 包含用户基础资料和业务规则校验。
 */
@Getter
public class User {

    private static final Set<String> VALID_GENDERS = Set.of("UNKNOWN", "MALE", "FEMALE");
    private static final int NICKNAME_MIN_LENGTH = 2;
    private static final int NICKNAME_MAX_LENGTH = 32;

    private Long id;
    private PhoneNumber phone;
    private String nickname;
    private String avatarUrl;
    private Gender gender;
    private LocalDate birthday;
    private Role role;
    private LocalDateTime createdAt;

    private User() {
    }

    /**
     * 创建新用户（注册场景）。
     */
    public static User create(PhoneNumber phone, String nickname, Role role) {
        User user = new User();
        user.phone = phone;
        user.nickname = nickname;
        user.gender = Gender.UNKNOWN;
        user.role = role;
        return user;
    }

    /**
     * 从持久化数据恢复用户对象。
     */
    public static User restore(Long id, PhoneNumber phone, String nickname, String avatarUrl,
                               Gender gender, LocalDate birthday, Role role, LocalDateTime createdAt) {
        User user = new User();
        user.id = id;
        user.phone = phone;
        user.nickname = nickname;
        user.avatarUrl = avatarUrl;
        user.gender = gender == null ? Gender.UNKNOWN : gender;
        user.birthday = birthday;
        user.role = role;
        user.createdAt = createdAt;
        return user;
    }

    /**
     * 更新用户资料，包含业务规则校验。
     *
     * @throws IllegalArgumentException 当昵称、性别或生日不合法时
     */
    public void updateProfile(String nickname, String avatarUrl, String genderStr, LocalDate birthday) {
        validateNickname(nickname);
        validateGender(genderStr);
        validateBirthday(birthday);

        this.nickname = nickname.trim();
        this.avatarUrl = blankToNull(avatarUrl);
        this.gender = Gender.of(genderStr);
        this.birthday = birthday;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().length() < NICKNAME_MIN_LENGTH
                || nickname.trim().length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("PROFILE_NICKNAME_INVALID");
        }
    }

    private void validateGender(String genderStr) {
        if (genderStr != null && !genderStr.trim().isEmpty() && !VALID_GENDERS.contains(genderStr.trim())) {
            throw new IllegalArgumentException("PROFILE_GENDER_INVALID");
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday != null && birthday.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("PROFILE_BIRTHDAY_INVALID");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
