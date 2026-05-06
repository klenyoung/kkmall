package com.kkmall.order.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.common.interfaces.PageResult;
import com.kkmall.order.application.OrderApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    private final OrderApplicationService service;

    public AdminOrderController(OrderApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(@RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(service.adminOrders(status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(service.adminOrderDetail(id));
    }
}
