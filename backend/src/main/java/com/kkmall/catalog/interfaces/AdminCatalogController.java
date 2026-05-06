package com.kkmall.catalog.interfaces;

import com.kkmall.catalog.application.AdminCatalogApplicationService;
import com.kkmall.common.interfaces.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminCatalogController {
    private final AdminCatalogApplicationService service;

    public AdminCatalogController(AdminCatalogApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> products(@RequestParam(required = false) String status) {
        return ApiResponse.ok(service.products(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.productDetail(id));
    }

    @GetMapping("/{id}/detail-config")
    public ApiResponse<Map<String, Object>> detailConfig(@PathVariable Long id) {
        return ApiResponse.ok(service.detailConfig(id));
    }

    @PutMapping("/{id}/detail-config")
    public ApiResponse<Map<String, Object>> saveDetailConfig(@PathVariable Long id, @RequestBody AdminCatalogApplicationService.DetailConfigRequest request) {
        return ApiResponse.ok(service.saveDetailConfig(id, request));
    }

    @PostMapping("/{id}/reviews")
    public ApiResponse<Map<String, Object>> createReview(@PathVariable Long id, @RequestBody AdminCatalogApplicationService.ReviewRequest request) {
        return ApiResponse.ok(service.saveReview(id, null, request));
    }

    @PutMapping("/{id}/reviews/{reviewId}")
    public ApiResponse<Map<String, Object>> updateReview(@PathVariable Long id, @PathVariable Long reviewId, @RequestBody AdminCatalogApplicationService.ReviewRequest request) {
        return ApiResponse.ok(service.saveReview(id, reviewId, request));
    }

    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ApiResponse<Map<String, Object>> deleteReview(@PathVariable Long id, @PathVariable Long reviewId) {
        return ApiResponse.ok(service.deleteReview(id, reviewId));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody AdminCatalogApplicationService.ProductRequest request) {
        return ApiResponse.ok(service.saveProduct(null, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody AdminCatalogApplicationService.ProductRequest request) {
        return ApiResponse.ok(service.saveProduct(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable Long id, @RequestBody ProductStatusRequest request) {
        return ApiResponse.ok(service.updateStatus(id, request.status));
    }

    public static class ProductStatusRequest {
        public String status;
    }
}
