package com.kkmall.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID_PENDING_SHIPMENT,
    SHIPPED,
    COMPLETED,
    CANCELLED
}
