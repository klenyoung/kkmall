package com.kkmall.payment.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付结果响应 DTO。
 */
@Data
public class PayResultDto {

    private Long orderId;

    private String status;

    private LocalDateTime paidAt;
}
