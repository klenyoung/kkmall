package com.kkmall.cart.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车项表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart_items")
public class CartItemPo extends BasePo {

    @TableId
    private Long id;

    private Long userId;

    private Long productId;

    private Long skuId;

    private Integer quantity;
}
