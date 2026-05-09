package com.kkmall.fulfillment.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 物流发货表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shipments")
public class ShipmentPo extends BasePo {

    @TableId
    private Long id;

    private Long orderId;

    private String logisticsCompany;

    private String trackingNo;

    private LocalDateTime shippedAt;
}
