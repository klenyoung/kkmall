package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

import java.time.LocalDateTime;

@TableName("product_promotion_texts")
public class ProductPromotionTextPo extends BasePo {
    @TableId
    public Long id;
    public Long productId;
    public String title;
    public String description;
    public String label;
    public LocalDateTime startAt;
    public LocalDateTime endAt;
    public Integer sortOrder;
    public Integer enabled;
}
