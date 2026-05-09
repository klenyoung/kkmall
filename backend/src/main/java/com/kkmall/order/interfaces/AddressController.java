package com.kkmall.order.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.order.application.OrderApplicationService;
import com.kkmall.order.interfaces.dto.AddressDto;
import com.kkmall.order.interfaces.dto.IdResultDto;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private final OrderApplicationService service;

    public AddressController(OrderApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<AddressDto>> list() {
        return ApiResponse.ok(service.addresses(SecurityUtils.currentUserId()));
    }

    @PostMapping
    public ApiResponse<IdResultDto> create(@RequestBody OrderApplicationService.AddressRequest request) {
        return ApiResponse.ok(service.createAddress(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/{id}")
    public ApiResponse<IdResultDto> update(@PathVariable Long id, @RequestBody OrderApplicationService.AddressRequest request) {
        return ApiResponse.ok(service.updateAddress(SecurityUtils.currentUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<IdResultDto> delete(@PathVariable Long id) {
        return ApiResponse.ok(service.deleteAddress(SecurityUtils.currentUserId(), id));
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<IdResultDto> setDefault(@PathVariable Long id) {
        return ApiResponse.ok(service.setDefaultAddress(SecurityUtils.currentUserId(), id));
    }
}
