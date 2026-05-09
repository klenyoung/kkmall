package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价 DTO。
 */
@Data
public class ReviewDto {

    private Long id;
    private Long skuId;
    private String userNickname;
    private Integer rating;
    private String content;
    private List<String> imageUrls;
    private List<String> tags;
    private LocalDateTime reviewedAt;
}
