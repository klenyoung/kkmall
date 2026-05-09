package com.kkmall.catalog.interfaces;

import com.kkmall.catalog.application.AdminCatalogApplicationService;
import com.kkmall.catalog.interfaces.dto.AdminProductCardDto;
import com.kkmall.catalog.interfaces.dto.DetailConfigDto;
import com.kkmall.catalog.interfaces.dto.IdResultDto;
import com.kkmall.catalog.interfaces.dto.ProductDetailDto;
import com.kkmall.catalog.interfaces.dto.StatusResultDto;
import com.kkmall.common.interfaces.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminCatalogController {
    private final AdminCatalogApplicationService service;

    public AdminCatalogController(AdminCatalogApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AdminProductCardDto>> products(@RequestParam(required = false) String status) {
        return ApiResponse.ok(service.products(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.productDetail(id));
    }

    @GetMapping("/{id}/detail-config")
    public ApiResponse<DetailConfigDto> detailConfig(@PathVariable Long id) {
        return ApiResponse.ok(service.detailConfig(id));
    }

    @PutMapping("/{id}/detail-config")
    public ApiResponse<IdResultDto> saveDetailConfig(@PathVariable Long id, @RequestBody AdminCatalogApplicationService.DetailConfigRequest request) {
        return ApiResponse.ok(service.saveDetailConfig(id, request));
    }

    @PostMapping("/{id}/reviews")
    public ApiResponse<IdResultDto> createReview(@PathVariable Long id, @RequestBody AdminCatalogApplicationService.ReviewRequest request) {
        return ApiResponse.ok(service.saveReview(id, null, request));
    }

    @PutMapping("/{id}/reviews/{reviewId}")
    public ApiResponse<IdResultDto> updateReview(@PathVariable Long id, @PathVariable Long reviewId, @RequestBody AdminCatalogApplicationService.ReviewRequest request) {
        return ApiResponse.ok(service.saveReview(id, reviewId, request));
    }

    @DeleteMapping("/{id}/reviews/{reviewId}")
    public ApiResponse<IdResultDto> deleteReview(@PathVariable Long id, @PathVariable Long reviewId) {
        return ApiResponse.ok(service.deleteReview(id, reviewId));
    }

    @PostMapping
    public ApiResponse<IdResultDto> create(@RequestBody AdminCatalogApplicationService.ProductRequest request) {
        return ApiResponse.ok(service.saveProduct(null, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<IdResultDto> update(@PathVariable Long id, @RequestBody AdminCatalogApplicationService.ProductRequest request) {
        return ApiResponse.ok(service.saveProduct(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<StatusResultDto> status(@PathVariable Long id, @RequestBody ProductStatusRequest request) {
        return ApiResponse.ok(service.updateStatus(id, request.status));
    }

    public static class ProductStatusRequest {
        public String status;
    }
}
