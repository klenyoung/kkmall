package com.kkmall.catalog.interfaces;

import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.catalog.interfaces.dto.CategoryDto;
import com.kkmall.catalog.interfaces.dto.ProductCardDto;
import com.kkmall.catalog.interfaces.dto.ProductDetailDto;
import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.common.interfaces.PageResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台商品目录接口。
 */
@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogApplicationService catalogApplicationService;

    public CatalogController(CatalogApplicationService catalogApplicationService) {
        this.catalogApplicationService = catalogApplicationService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> categories() {
        return ApiResponse.ok(catalogApplicationService.categories());
    }

    @GetMapping("/products")
    public ApiResponse<PageResult<ProductCardDto>> products(@RequestParam(required = false) Long categoryId,
                                                            @RequestParam(required = false) String keyword,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(catalogApplicationService.products(categoryId, keyword, page, pageSize));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductDetailDto> product(@PathVariable Long id) {
        return ApiResponse.ok(catalogApplicationService.productDetail(id));
    }
}
