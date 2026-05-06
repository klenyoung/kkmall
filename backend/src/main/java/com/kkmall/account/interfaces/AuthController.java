package com.kkmall.account.interfaces;

import com.kkmall.account.application.AuthApplicationService;
import com.kkmall.common.interfaces.ApiResponse;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/auth/mock-code")
    public ApiResponse<Map<String, Object>> mockCode(@Valid @RequestBody PhoneRequest request) {
        return ApiResponse.ok(authApplicationService.mockCode(request.phone));
    }

    @PostMapping("/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authApplicationService.login(request.phone, request.code));
    }

    @PostMapping("/admin/auth/login")
    public ApiResponse<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authApplicationService.adminLogin(request.phone, request.code));
    }

    public static class PhoneRequest {
        @NotBlank public String phone;
    }

    public static class LoginRequest {
        @NotBlank public String phone;
        @NotBlank public String code;
    }
}
