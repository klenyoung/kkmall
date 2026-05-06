package com.kkmall.cart.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("cart_items")
public class CartItemPo extends BasePo {
    @TableId
    public Long id;
    public Long userId;
    public Long productId;
    public Long skuId;
    public Integer quantity;
}
