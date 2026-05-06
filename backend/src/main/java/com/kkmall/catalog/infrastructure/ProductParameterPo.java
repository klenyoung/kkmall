package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("product_parameters")
public class ProductParameterPo extends BasePo {
    @TableId
    public Long id;
    public Long productId;
    public String name;
    public String value;
    public Integer sortOrder;
    public Integer enabled;
}
