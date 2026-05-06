package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

import java.time.LocalDateTime;

@TableName("orders")
public class OrderPo extends BasePo {
    @TableId
    public Long id;
    public String orderNo;
    public Long userId;
    public Long productAmount;
    public Long shippingFee;
    public Long payableAmount;
    public String status;
    public String addressSnapshot;
    public LocalDateTime paidAt;
    public LocalDateTime shippedAt;
    public LocalDateTime completedAt;
    public LocalDateTime cancelledAt;
}
