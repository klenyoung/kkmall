package com.kkmall.payment.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.order.domain.OrderStatus;
import com.kkmall.order.infrastructure.OrderItemMapper;
import com.kkmall.order.infrastructure.OrderItemPo;
import com.kkmall.order.infrastructure.OrderMapper;
import com.kkmall.order.infrastructure.OrderPo;
import com.kkmall.catalog.application.CatalogApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentApplicationService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;

    public PaymentApplicationService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, SkuMapper skuMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.skuMapper = skuMapper;
    }

    @Transactional
    public Map<String, Object> mockPay(Long userId, Long orderId) {
        OrderPo order = orderMapper.selectById(orderId);
        if (order == null || !order.userId.equals(userId)) throw new BusinessException("ORDER_NOT_FOUND");
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.status)) {
            if (OrderStatus.PAID_PENDING_SHIPMENT.name().equals(order.status) || OrderStatus.SHIPPED.name().equals(order.status) || OrderStatus.COMPLETED.name().equals(order.status)) {
                return CatalogApplicationService.mapOf("orderId", order.id, "status", order.status, "paidAt", order.paidAt);
            }
            throw new BusinessException("ORDER_STATUS_INVALID");
        }
        List<OrderItemPo> items = orderItemMapper.selectList(new QueryWrapper<OrderItemPo>().eq("order_id", order.id));
        for (OrderItemPo item : items) {
            int updated = skuMapper.decreaseStock(item.skuId, item.quantity);
            if (updated != 1) throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
        }
        order.status = OrderStatus.PAID_PENDING_SHIPMENT.name();
        order.paidAt = LocalDateTime.now();
        orderMapper.updateById(order);
        return CatalogApplicationService.mapOf("orderId", order.id, "status", order.status, "paidAt", order.paidAt);
    }

    public static class MockPayRequest {
        public Long orderId;
    }
}
