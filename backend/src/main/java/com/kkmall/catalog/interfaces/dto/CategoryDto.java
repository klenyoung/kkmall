package com.kkmall.catalog.interfaces.dto;

import lombok.Data;

/**
 * 分类响应 DTO。
 */
@Data
public class CategoryDto {

    private Long id;

    private String name;

    private Integer sortOrder;

    private Integer enabled;
}
