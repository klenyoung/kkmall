package com.kkmall.order.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.order.application.OrderApplicationService;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private final OrderApplicationService service;

    public AddressController(OrderApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(service.addresses(SecurityUtils.currentUserId()));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody OrderApplicationService.AddressRequest request) {
        return ApiResponse.ok(service.createAddress(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody OrderApplicationService.AddressRequest request) {
        return ApiResponse.ok(service.updateAddress(SecurityUtils.currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable Long id) {
        return ApiResponse.ok(service.deleteAddress(SecurityUtils.currentUserId(), id));
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<Map<String, Object>> setDefault(@PathVariable Long id) {
        return ApiResponse.ok(service.setDefaultAddress(SecurityUtils.currentUserId(), id));
    }
}
