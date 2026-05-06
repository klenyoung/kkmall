package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("order_items")
public class OrderItemPo extends BasePo {
    @TableId
    public Long id;
    public Long orderId;
    public Long productId;
    public Long skuId;
    public String titleSnapshot;
    public String imageSnapshot;
    public String skuSnapshot;
    public Long unitPrice;
    public Integer quantity;
    public Long subtotal;
}
