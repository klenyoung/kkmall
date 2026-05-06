package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("product_service_promises")
public class ProductServicePromisePo extends BasePo {
    @TableId
    public Long id;
    public Long productId;
    public String title;
    public String description;
    public String icon;
    public Integer sortOrder;
    public Integer enabled;
}
