package com.kkmall.payment.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.payment.application.PaymentApplicationService;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentApplicationService service;

    public PaymentController(PaymentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/mock")
    public ApiResponse<Map<String, Object>> mock(@RequestBody PaymentApplicationService.MockPayRequest request) {
        return ApiResponse.ok(service.mockPay(SecurityUtils.currentUserId(), request.orderId));
    }
}
