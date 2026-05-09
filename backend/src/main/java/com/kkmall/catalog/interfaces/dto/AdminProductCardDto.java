package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * 后台商品列表卡片 DTO。
 */
@Data
public class AdminProductCardDto {

    private Long id;
    private String title;
    private String brand;
    private String subtitle;
    private Long categoryId;
    private String status;
    private List<String> images;
    private String mainImage;
    private String coverImage;
    private Long minPrice;
    private Integer totalStock;
    private Integer salesCount;
    private Integer sortOrder;
}
