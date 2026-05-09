package com.kkmall.fulfillment.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发货结果响应 DTO。
 */
@Data
public class ShipResultDto {

    private Long orderId;

    private String status;

    private ShipmentInfo shipment;

    @Data
    public static class ShipmentInfo {
        private String logisticsCompany;
        private String trackingNo;
        private LocalDateTime shippedAt;
    }
}
