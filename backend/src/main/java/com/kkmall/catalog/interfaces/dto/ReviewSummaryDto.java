package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

import java.util.List;

/**
 * 评价摘要 DTO。
 */
@Data
public class ReviewSummaryDto {

    private Integer reviewCount;
    private Integer goodRate;
    private Double averageRating;
    private List<String> tags;
}
