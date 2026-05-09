package com.kkmall.order.interfaces.dto;

import lombok.Data;

/**
 * 收货地址响应 DTO。
 */
@Data
public class AddressDto {

    private Long id;

    private String receiverName;

    private String receiverPhone;

    private String region;

    private String detail;

    private Integer isDefault;
}
