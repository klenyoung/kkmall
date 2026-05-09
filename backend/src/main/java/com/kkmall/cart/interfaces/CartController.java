package com.kkmall.cart.interfaces;

import com.kkmall.cart.application.CartApplicationService;
import com.kkmall.cart.interfaces.dto.CartOperationResultDto;
import com.kkmall.cart.interfaces.dto.CartViewDto;
import com.kkmall.cart.interfaces.dto.DeleteResultDto;
import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.security.SecurityUtils;
import lombok.Data;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车接口。
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartApplicationService cartApplicationService;

    public CartController(CartApplicationService cartApplicationService) {
        this.cartApplicationService = cartApplicationService;
    }

    @GetMapping
    public ApiResponse<CartViewDto> list() {
        return ApiResponse.ok(cartApplicationService.list(SecurityUtils.currentUserId()));
    }

    @PostMapping("/items")
    public ApiResponse<CartOperationResultDto> add(@RequestBody AddCartRequest request) {
        return ApiResponse.ok(cartApplicationService.add(
                SecurityUtils.currentUserId(), request.getSkuId(), request.getQuantity()));
    }

    @PatchMapping("/items/{id}")
    public ApiResponse<CartOperationResultDto> update(@PathVariable Long id, @RequestBody UpdateCartRequest request) {
        return ApiResponse.ok(cartApplicationService.update(
                SecurityUtils.currentUserId(), id, request.getQuantity()));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<DeleteResultDto> delete(@PathVariable Long id) {
        cartApplicationService.delete(SecurityUtils.currentUserId(), id);
        return ApiResponse.ok(DeleteResultDto.success());
    }

    @Data
    public static class AddCartRequest {
        private Long skuId;
        private int quantity;
    }

    @Data
    public static class UpdateCartRequest {
        private int quantity;
    }
}
