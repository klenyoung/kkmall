package com.kkmall.cart.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * 购物车视图 DTO。
 */
@Data
public class CartViewDto {

    private List<CartItemDto> items;

    private Long productAmount;
}
