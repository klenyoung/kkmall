package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("products")
public class ProductPo extends BasePo {

    @TableId
    private Long id;

    private Long categoryId;

    private String title;

    private String brand;

    private String subtitle;

    private String description;

    private String sellingPoints;

    private String unit;

    private String detailHtml;

    private String attributes;

    private String mainImage;

    private Integer salesCount;

    private Integer sortOrder;

    private String images;

    private String status;
}
