package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("skus")
public class SkuPo extends BasePo {
    @TableId
    public Long id;
    public Long productId;
    public String skuCode;
    public String specName;
    public String specValue;
    public String specs;
    public String imageUrl;
    public Long price;
    public Long marketPrice;
    public Long costPrice;
    public Integer stock;
    public Integer weightGrams;
    public String barcode;
    public Integer enabled;
}
