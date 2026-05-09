package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class OrderPo extends BasePo {

    @TableId
    private Long id;

    private String orderNo;

    private Long userId;

    private Long productAmount;

    private Long shippingFee;

    private Long payableAmount;

    private String status;

    private String addressSnapshot;

    private LocalDateTime paidAt;

    private LocalDateTime shippedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;
}
