package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情配置 DTO（后台查询用）。
 */
@Data
public class DetailConfigDto {

    private ProductDetailDto product;
    private List<AdminParameterDto> parameters;
    private List<AdminServicePromiseDto> servicePromises;
    private List<AdminPromotionDto> promotions;
    private List<AdminReviewDto> reviews;
    private List<Long> storeRecommendationIds;
    private List<Long> relatedRecommendationIds;

    @Data
    public static class AdminParameterDto {
        private Long id;
        private String name;
        private String value;
        private Integer sortOrder;
        private Integer enabled;
    }

    @Data
    public static class AdminServicePromiseDto {
        private Long id;
        private String title;
        private String description;
        private String icon;
        private Integer sortOrder;
        private Integer enabled;
    }

    @Data
    public static class AdminPromotionDto {
        private Long id;
        private String title;
        private String description;
        private String label;
        private LocalDateTime startAt;
        private LocalDateTime endAt;
        private Integer sortOrder;
        private Integer enabled;
    }

    @Data
    public static class AdminReviewDto {
        private Long id;
        private Long skuId;
        private String userNickname;
        private Integer rating;
        private String content;
        private List<String> imageUrls;
        private List<String> tags;
        private LocalDateTime reviewedAt;
        private String status;
        private Integer sortOrder;
    }
}
