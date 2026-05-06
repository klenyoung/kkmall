package com.kkmall.fulfillment.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

import java.time.LocalDateTime;

@TableName("shipments")
public class ShipmentPo extends BasePo {
    @TableId
    public Long id;
    public Long orderId;
    public String logisticsCompany;
    public String trackingNo;
    public LocalDateTime shippedAt;
}
