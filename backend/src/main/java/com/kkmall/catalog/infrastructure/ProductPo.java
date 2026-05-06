package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("products")
public class ProductPo extends BasePo {
    @TableId
    public Long id;
    public Long categoryId;
    public String title;
    public String brand;
    public String subtitle;
    public String description;
    public String sellingPoints;
    public String unit;
    public String detailHtml;
    public String attributes;
    public String mainImage;
    public Integer salesCount;
    public Integer sortOrder;
    public String images;
    public String status;
}
