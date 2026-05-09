package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU 表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skus")
public class SkuPo extends BasePo {

    @TableId
    private Long id;

    private Long productId;

    private String skuCode;

    private String specName;

    private String specValue;

    private String specs;

    private String imageUrl;

    private Long price;

    private Long marketPrice;

    private Long costPrice;

    private Integer stock;

    private Integer weightGrams;

    private String barcode;

    private Integer enabled;
}
