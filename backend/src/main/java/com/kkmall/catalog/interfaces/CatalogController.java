package com.kkmall.catalog.interfaces;

import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.common.interfaces.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {
    private final CatalogApplicationService catalogApplicationService;

    public CatalogController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Map<String, Object>>> categories() {
        return ApiResponse.ok(catalogApplicationService.categories());
    }

    @GetMapping("/products")
    public ApiResponse<PageResult<Map<String, Object>>> products(@RequestParam(required = false) Long categoryId,
                                                                 @RequestParam(required = false) String keyword,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(catalogApplicationService.products(categoryId, keyword, page, pageSize));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<Map<String, Object>> product(@PathVariable Long id) {
        return ApiResponse.ok(catalogApplicationService.productDetail(id));
    }
}
