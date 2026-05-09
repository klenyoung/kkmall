package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单项表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_items")
public class OrderItemPo extends BasePo {

    @TableId
    private Long id;

    private Long orderId;

    private Long productId;

    private Long skuId;

    private String titleSnapshot;

    private String imageSnapshot;

    private String skuSnapshot;

    private Long unitPrice;

    private Integer quantity;

    private Long subtotal;
}
