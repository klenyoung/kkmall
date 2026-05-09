package com.kkmall.account.interfaces;

import com.kkmall.account.application.AccountProfileApplicationService;
import com.kkmall.account.interfaces.dto.UploadResultDto;
import com.kkmall.account.interfaces.dto.UserProfileDto;
import com.kkmall.catalog.application.ImageUploadApplicationService;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户资料和头像管理接口。
 */
@RestController
@RequestMapping("/api/v1/account")
public class AccountProfileController {

    private static final long AVATAR_MAX_SIZE = 2L * 1024L * 1024L;
    private final AccountProfileApplicationService profileService;
    private final ImageUploadApplicationService uploadService;

    public AccountProfileController(AccountProfileApplicationService profileService, ImageUploadApplicationService uploadService) {
        this.profileService = profileService;
        this.uploadService = uploadService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserProfileDto> profile() {
        return ApiResponse.ok(profileService.profile(SecurityUtils.currentUserId()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileDto> updateProfile(@RequestBody AccountProfileApplicationService.ProfileRequest request) {
        return ApiResponse.ok(profileService.updateProfile(SecurityUtils.currentUserId(), request));
    }

    @PostMapping("/avatar")
    public ApiResponse<UploadResultDto> avatar(@RequestParam("file") MultipartFile file) {
        if (file != null && file.getSize() > AVATAR_MAX_SIZE) {
            throw new BusinessException("UPLOAD_FILE_TOO_LARGE");
        }
        Map<String, Object> result = uploadService.uploadImage(file, "avatars");
        UploadResultDto dto = new UploadResultDto();
        dto.setUrl((String) result.get("url"));
        dto.setPublicUrl((String) result.get("publicUrl"));
        dto.setObjectName((String) result.get("objectName"));
        return ApiResponse.ok(dto);
    }
}
