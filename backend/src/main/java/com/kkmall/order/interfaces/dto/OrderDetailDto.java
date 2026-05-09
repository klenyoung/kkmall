package com.kkmall.order.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单详情 DTO。
 */
@Data
public class OrderDetailDto {

    private Long id;

    private String orderNo;

    private Long userId;

    private Long productAmount;

    private Long shippingFee;

    private Long payableAmount;

    private String status;

    private Map<String, Object> addressSnapshot;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime shippedAt;

    private List<OrderItemDto> items;

    private ShipmentDto shipment;

    @Data
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private Long skuId;
        private String titleSnapshot;
        private String imageSnapshot;
        private String skuSnapshot;
        private Long unitPrice;
        private Integer quantity;
        private Long subtotal;
    }

    @Data
    public static class ShipmentDto {
        private String logisticsCompany;
        private String trackingNo;
        private LocalDateTime shippedAt;
    }
}
