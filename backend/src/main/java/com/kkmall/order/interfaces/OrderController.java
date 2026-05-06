package com.kkmall.order.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.common.interfaces.PageResult;
import com.kkmall.order.application.OrderApplicationService;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderApplicationService service;

    public OrderController(OrderApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody OrderApplicationService.CreateOrderRequest request) {
        return ApiResponse.ok(service.createOrder(SecurityUtils.currentUserId(), request));
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(@RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(service.userOrders(SecurityUtils.currentUserId(), status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.userOrderDetail(SecurityUtils.currentUserId(), id));
    }
}
