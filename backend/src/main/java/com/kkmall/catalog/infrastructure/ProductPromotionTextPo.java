package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品促销文案表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_promotion_texts")
public class ProductPromotionTextPo extends BasePo {

    @TableId
    private Long id;

    private Long productId;

    private String title;

    private String description;

    private String label;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer sortOrder;

    private Integer enabled;
}
