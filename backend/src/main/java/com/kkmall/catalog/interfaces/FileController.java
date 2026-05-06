package com.kkmall.catalog.interfaces;

import com.kkmall.catalog.application.ImageUploadApplicationService;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {
    private final ImageUploadApplicationService service;

    public FileController(ImageUploadApplicationService service) {
        this.service = service;
    }

    @GetMapping("/{*objectName}")
    public void file(@PathVariable String objectName, HttpServletResponse response) throws Exception {
        String normalized = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        try (InputStream object = service.getObject(normalized)) {
            response.setContentType(contentType(normalized));
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            StreamUtils.copy(object, response.getOutputStream());
        }
    }

    private String contentType(String objectName) {
        String name = objectName.toLowerCase();
        if (name.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        return MediaType.IMAGE_JPEG_VALUE;
    }
}
