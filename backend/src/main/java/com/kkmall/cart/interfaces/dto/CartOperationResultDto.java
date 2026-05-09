package com.kkmall.cart.interfaces.dto;

import lombok.Data;

/**
 * 购物车操作结果 DTO（加购/更新数量）。
 */
@Data
public class CartOperationResultDto {

    private Long id;

    private Long skuId;

    private Integer quantity;
}
