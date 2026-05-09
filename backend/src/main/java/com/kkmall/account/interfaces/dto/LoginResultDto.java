package com.kkmall.account.interfaces.dto;

import lombok.Data;

/**
 * 登录成功响应 DTO，包含 token 和用户摘要。
 */
@Data
public class LoginResultDto {

    private String token;

    private UserProfileDto user;
}
