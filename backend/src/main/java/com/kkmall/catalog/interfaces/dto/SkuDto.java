package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

import java.util.Map;

/**
 * SKU 响应 DTO。
 */
@Data
public class SkuDto {

    private Long id;
    private String skuCode;
    private String specName;
    private String specValue;
    private Map<String, Object> specs;
    private String imageUrl;
    private Long price;
    private Long marketPrice;
    private Long originPrice;
    private Long costPrice;
    private Integer stock;
    private Integer weightGrams;
    private String barcode;
    private Integer enabled;
    private Boolean sellable;
}
