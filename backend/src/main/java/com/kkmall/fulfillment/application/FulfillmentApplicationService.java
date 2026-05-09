package com.kkmall.fulfillment.application;

import com.kkmall.common.exception.BusinessException;
import com.kkmall.fulfillment.infrastructure.ShipmentMapper;
import com.kkmall.fulfillment.infrastructure.ShipmentPo;
import com.kkmall.fulfillment.interfaces.dto.ShipResultDto;
import com.kkmall.order.domain.OrderStatus;
import com.kkmall.order.infrastructure.OrderMapper;
import com.kkmall.order.infrastructure.OrderPo;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 履约应用服务。
 */
@Service
public class FulfillmentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FulfillmentApplicationService.class);

    private final OrderMapper orderMapper;
    private final ShipmentMapper shipmentMapper;

    public FulfillmentApplicationService(OrderMapper orderMapper, ShipmentMapper shipmentMapper) {
        this.orderMapper = orderMapper;
        this.shipmentMapper = shipmentMapper;
    }

    @Transactional
    public ShipResultDto ship(Long orderId, ShipmentRequest request) {
        OrderPo order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("ORDER_NOT_FOUND");
        }
        if (!OrderStatus.PAID_PENDING_SHIPMENT.name().equals(order.getStatus())) {
            throw new BusinessException("ORDER_STATUS_INVALID");
        }

        ShipmentPo shipment = new ShipmentPo();
        shipment.setOrderId(orderId);
        shipment.setLogisticsCompany(request.getLogisticsCompany());
        shipment.setTrackingNo(request.getTrackingNo());
        shipment.setShippedAt(LocalDateTime.now());
        shipmentMapper.insert(shipment);

        order.setStatus(OrderStatus.SHIPPED.name());
        order.setShippedAt(shipment.getShippedAt());
        orderMapper.updateById(order);

        log.info("订单发货成功，orderId={}, logisticsCompany={}, trackingNo={}",
                orderId, shipment.getLogisticsCompany(), shipment.getTrackingNo());

        ShipResultDto result = new ShipResultDto();
        result.setOrderId(orderId);
        result.setStatus(order.getStatus());
        ShipResultDto.ShipmentInfo info = new ShipResultDto.ShipmentInfo();
        info.setLogisticsCompany(shipment.getLogisticsCompany());
        info.setTrackingNo(shipment.getTrackingNo());
        info.setShippedAt(shipment.getShippedAt());
        result.setShipment(info);
        return result;
    }

    @Data
    public static class ShipmentRequest {
        private String logisticsCompany;
        private String trackingNo;
    }
}
