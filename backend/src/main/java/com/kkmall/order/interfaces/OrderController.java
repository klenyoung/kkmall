package com.kkmall.order.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.common.interfaces.PageResult;
import com.kkmall.order.application.OrderApplicationService;
import com.kkmall.order.interfaces.dto.CreateOrderResultDto;
import com.kkmall.order.interfaces.dto.OrderDetailDto;
import com.kkmall.order.interfaces.dto.OrderSummaryDto;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderApplicationService service;

    public OrderController(OrderApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<CreateOrderResultDto> create(@RequestBody OrderApplicationService.CreateOrderRequest request) {
        return ApiResponse.ok(service.createOrder(SecurityUtils.currentUserId(), request));
    }

    @GetMapping
    public ApiResponse<PageResult<OrderSummaryDto>> list(@RequestParam(required = false) String status,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(service.userOrders(SecurityUtils.currentUserId(), status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.userOrderDetail(SecurityUtils.currentUserId(), id));
    }
}
