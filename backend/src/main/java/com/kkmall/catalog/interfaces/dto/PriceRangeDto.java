package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

/**
 * 价格区间 DTO。
 */
@Data
public class PriceRangeDto {

    private Long minPrice;
    private Long maxPrice;
    private String currency;
}
