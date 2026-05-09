package com.kkmall.account.interfaces.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料响应 DTO。
 */
@Data
public class UserProfileDto {

    private Long id;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private String gender;

    private LocalDate birthday;

    private String role;

    private LocalDateTime createdAt;
}
