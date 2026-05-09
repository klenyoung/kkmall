package com.kkmall.cart.interfaces.dto;

import lombok.Data;

/**
 * 购物车项 DTO。
 */
@Data
public class CartItemDto {

    private Long id;

    private Long productId;

    private Long skuId;

    private String title;

    private String image;

    private String specText;

    private Long price;

    private Integer quantity;

    private Integer stock;

    private Long subtotal;

    private Boolean settleable;

    private String unsettleableReason;
}
