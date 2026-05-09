package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品参数表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_parameters")
public class ProductParameterPo extends BasePo {

    @TableId
    private Long id;

    private Long productId;

    private String name;

    private String value;

    private Integer sortOrder;

    private Integer enabled;
}
