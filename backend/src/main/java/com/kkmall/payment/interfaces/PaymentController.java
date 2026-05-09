package com.kkmall.payment.interfaces;

import com.kkmall.common.interfaces.ApiResponse;
import com.kkmall.payment.application.PaymentApplicationService;
import com.kkmall.payment.interfaces.dto.PayResultDto;
import com.kkmall.security.SecurityUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口。
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentApplicationService service;

    public PaymentController(PaymentApplicationService service) {
        this.service = service;
    }

    @PostMapping("/mock")
    public ApiResponse<PayResultDto> mock(@RequestBody PaymentApplicationService.MockPayRequest request) {
        return ApiResponse.ok(service.mockPay(SecurityUtils.currentUserId(), request.getOrderId()));
    }
}
