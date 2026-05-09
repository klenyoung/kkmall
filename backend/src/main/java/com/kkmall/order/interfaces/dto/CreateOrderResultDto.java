package com.kkmall.order.interfaces.dto;

import lombok.Data;

/**
 * 创建订单结果 DTO。
 */
@Data
public class CreateOrderResultDto {

    private Long id;

    private String orderNo;

    private String status;

    private Long productAmount;

    private Long shippingFee;

    private Long payableAmount;
}
