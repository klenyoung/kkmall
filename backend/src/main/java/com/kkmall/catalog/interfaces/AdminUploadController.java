package com.kkmall.catalog.interfaces;

import com.kkmall.catalog.application.ImageUploadApplicationService;
import com.kkmall.common.interfaces.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/uploads")
public class AdminUploadController {
    private final ImageUploadApplicationService service;

    public AdminUploadController(ImageUploadApplicationService service) {
        this.service = service;
    }

    @PostMapping("/images")
    public ApiResponse<Map<String, Object>> image(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(service.uploadImage(file));
    }
}
