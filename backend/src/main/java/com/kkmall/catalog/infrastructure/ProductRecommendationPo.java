package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("product_recommendations")
public class ProductRecommendationPo extends BasePo {
    @TableId
    public Long id;
    public Long sourceProductId;
    public Long targetProductId;
    public String scene;
    public Integer sortOrder;
    public Integer enabled;
}
