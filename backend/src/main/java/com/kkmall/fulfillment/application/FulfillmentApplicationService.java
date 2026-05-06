package com.kkmall.fulfillment.application;

import com.kkmall.catalog.application.CatalogApplicationService;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.fulfillment.infrastructure.ShipmentMapper;
import com.kkmall.fulfillment.infrastructure.ShipmentPo;
import com.kkmall.order.domain.OrderStatus;
import com.kkmall.order.infrastructure.OrderMapper;
import com.kkmall.order.infrastructure.OrderPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class FulfillmentApplicationService {
    private final OrderMapper orderMapper;
    private final ShipmentMapper shipmentMapper;

    public FulfillmentApplicationService(OrderMapper orderMapper, ShipmentMapper shipmentMapper) {
        this.orderMapper = orderMapper;
        this.shipmentMapper = shipmentMapper;
    }

    @Transactional
    public Map<String, Object> ship(Long orderId, ShipmentRequest request) {
        OrderPo order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("ORDER_NOT_FOUND");
        if (!OrderStatus.PAID_PENDING_SHIPMENT.name().equals(order.status)) throw new BusinessException("ORDER_STATUS_INVALID");
        ShipmentPo shipment = new ShipmentPo();
        shipment.orderId = orderId;
        shipment.logisticsCompany = request.logisticsCompany;
        shipment.trackingNo = request.trackingNo;
        shipment.shippedAt = LocalDateTime.now();
        shipmentMapper.insert(shipment);
        order.status = OrderStatus.SHIPPED.name();
        order.shippedAt = shipment.shippedAt;
        orderMapper.updateById(order);
        return CatalogApplicationService.mapOf("orderId", orderId, "status", order.status, "shipment", CatalogApplicationService.mapOf("logisticsCompany", shipment.logisticsCompany, "trackingNo", shipment.trackingNo, "shippedAt", shipment.shippedAt));
    }

    public static class ShipmentRequest {
        public String logisticsCompany;
        public String trackingNo;
    }
}
