package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品推荐配置表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_recommendations")
public class ProductRecommendationPo extends BasePo {

    @TableId
    private Long id;

    private Long sourceProductId;

    private Long targetProductId;

    private String scene;

    private Integer sortOrder;

    private Integer enabled;
}
