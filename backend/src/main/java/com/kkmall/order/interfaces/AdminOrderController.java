package com.kkmall.order.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.common.interfaces.PageResult;
import com.kkmall.order.application.OrderApplicationService;
import com.kkmall.order.interfaces.dto.OrderDetailDto;
import com.kkmall.order.interfaces.dto.OrderSummaryDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    private final OrderApplicationService service;

    public AdminOrderController(OrderApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<OrderSummaryDto>> list(@RequestParam(required = false) String status,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(service.adminOrders(status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.adminOrderDetail(id));
    }
}
