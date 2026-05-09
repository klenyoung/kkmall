package com.kkmall.payment.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.catalog.infrastructure.SkuMapper;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.order.domain.OrderStatus;
import com.kkmall.order.infrastructure.OrderItemMapper;
import com.kkmall.order.infrastructure.OrderItemPo;
import com.kkmall.order.infrastructure.OrderMapper;
import com.kkmall.order.infrastructure.OrderPo;
import com.kkmall.payment.interfaces.dto.PayResultDto;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付应用服务。
 */
@Service
public class PaymentApplicationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SkuMapper skuMapper;

    public PaymentApplicationService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, SkuMapper skuMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.skuMapper = skuMapper;
    }

    @Transactional
    public PayResultDto mockPay(Long userId, Long orderId) {
        OrderPo order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("ORDER_NOT_FOUND");
        }

        // 幂等处理：已支付/已发货/已完成的订单直接返回成功
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            if (OrderStatus.PAID_PENDING_SHIPMENT.name().equals(order.getStatus())
                    || OrderStatus.SHIPPED.name().equals(order.getStatus())
                    || OrderStatus.COMPLETED.name().equals(order.getStatus())) {
                return buildResult(order.getId(), order.getStatus(), order.getPaidAt());
            }
            throw new BusinessException("ORDER_STATUS_INVALID");
        }

        // 扣减库存
        List<OrderItemPo> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItemPo>().eq("order_id", order.getId()));
        for (OrderItemPo item : items) {
            int updated = skuMapper.decreaseStock(item.getSkuId(), item.getQuantity());
            if (updated != 1) {
                throw new BusinessException("SKU_STOCK_NOT_ENOUGH");
            }
        }

        // 使用乐观锁原子更新订单状态，防止并发重复支付
        LocalDateTime paidAt = LocalDateTime.now();
        int affected = orderMapper.compareAndUpdateStatus(
                order.getId(),
                OrderStatus.PENDING_PAYMENT.name(),
                OrderStatus.PAID_PENDING_SHIPMENT.name(),
                paidAt);
        if (affected != 1) {
            log.warn("并发支付竞争失败，orderId={}, userId={}", orderId, userId);
            throw new BusinessException("ORDER_STATUS_INVALID");
        }

        log.info("模拟支付成功，orderId={}, userId={}", orderId, userId);
        return buildResult(order.getId(), OrderStatus.PAID_PENDING_SHIPMENT.name(), paidAt);
    }

    private PayResultDto buildResult(Long orderId, String status, LocalDateTime paidAt) {
        PayResultDto dto = new PayResultDto();
        dto.setOrderId(orderId);
        dto.setStatus(status);
        dto.setPaidAt(paidAt);
        return dto;
    }

    @Data
    public static class MockPayRequest {
        private Long orderId;
    }
}
