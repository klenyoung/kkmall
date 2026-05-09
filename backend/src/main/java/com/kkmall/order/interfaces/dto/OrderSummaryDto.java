package com.kkmall.order.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单列表摘要 DTO。
 */
@Data
public class OrderSummaryDto {

    private Long id;

    private String orderNo;

    private Long productAmount;

    private Long shippingFee;

    private Long payableAmount;

    private String status;

    private LocalDateTime createdAt;
}
