package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

/**
 * 商品卡片 DTO（列表/推荐场景）。
 */
@Data
public class ProductCardDto {

    private Long id;

    private String title;

    private String brand;

    private String subtitle;

    private String coverImage;

    private String mainImage;

    private Long minPrice;

    private Integer salesCount;

    private String status;
}
