package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 商品详情完整 DTO。
 */
@Data
public class ProductDetailDto {

    private Long id;
    private Long categoryId;
    private String title;
    private String brand;
    private String subtitle;
    private String description;
    private List<String> sellingPoints;
    private String unit;
    private String detailHtml;
    private Map<String, Object> attributes;
    private List<String> images;
    private List<String> imageUrls;
    private String mainImage;
    private String mainImageUrl;
    private String coverImage;
    private Integer salesCount;
    private Integer sortOrder;
    private String status;
    private List<String> tags;
    private PriceRangeDto priceRange;
    private List<SkuDto> skus;
    private List<PromotionDto> promotions;
    private List<ServicePromiseDto> servicePromises;
    private List<ParameterDto> parameters;
    private ReviewSummaryDto reviewSummary;
    private List<ReviewDto> reviews;
    private List<ProductCardDto> storeRecommendations;
    private List<ProductCardDto> relatedRecommendations;
}
