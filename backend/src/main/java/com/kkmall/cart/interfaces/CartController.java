package com.kkmall.cart.interfaces;

import com.kkmall.cart.application.CartApplicationService;
import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartApplicationService cartApplicationService;

    public CartController(CartApplicationService cartApplicationService) {
        this.cartApplicationService = cartApplicationService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list() {
        return ApiResponse.ok(cartApplicationService.list(SecurityUtils.currentUserId()));
    }

    @PostMapping("/items")
    public ApiResponse<Map<String, Object>> add(@RequestBody AddCartRequest request) {
        return ApiResponse.ok(cartApplicationService.add(SecurityUtils.currentUserId(), request.skuId, request.quantity));
    }

    @PatchMapping("/items/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody UpdateCartRequest request) {
        return ApiResponse.ok(cartApplicationService.update(SecurityUtils.currentUserId(), id, request.quantity));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        cartApplicationService.delete(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok(CatalogDelete.deleted());
    }

    public static class AddCartRequest {
        public Long skuId;
        public int quantity;
    }

    public static class UpdateCartRequest {
        public int quantity;
    }

    static final class CatalogDelete {
        static Map<String, Object> deleted() {
            return com.kkmall.catalog.application.CatalogApplicationService.mapOf("deleted", true);
        }
    }
}
