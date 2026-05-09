package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

/**
 * 促销文案 DTO。
 */
@Data
public class PromotionDto {

    private Long id;
    private String title;
    private String description;
    private String label;
    private String type;
}
