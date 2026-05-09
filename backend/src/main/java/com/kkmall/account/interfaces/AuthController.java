package com.kkmall.account.interfaces;

import com.kkmall.account.application.AuthApplicationService;
import com.kkmall.account.interfaces.dto.LoginResultDto;
import com.kkmall.account.interfaces.dto.MockCodeResultDto;
import com.kkmall.common.interfaces.ApiResponse;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 认证接口：登录、模拟验证码。
 */
@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/auth/mock-code")
    public ApiResponse<MockCodeResultDto> mockCode(@Valid @RequestBody PhoneRequest request) {
        return ApiResponse.ok(authApplicationService.mockCode(request.getPhone()));
    }

    @PostMapping("/auth/login")
    public ApiResponse<LoginResultDto> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authApplicationService.login(request.getPhone(), request.getCode()));
    }

    @PostMapping("/admin/auth/login")
    public ApiResponse<LoginResultDto> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authApplicationService.adminLogin(request.getPhone(), request.getCode()));
    }

    @Data
    public static class PhoneRequest {
        @NotBlank
        private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String phone;
        @NotBlank
        private String code;
    }
}
