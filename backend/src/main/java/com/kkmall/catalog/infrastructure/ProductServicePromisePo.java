package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品服务承诺表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_service_promises")
public class ProductServicePromisePo extends BasePo {

    @TableId
    private Long id;

    private Long productId;

    private String title;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Integer enabled;
}
