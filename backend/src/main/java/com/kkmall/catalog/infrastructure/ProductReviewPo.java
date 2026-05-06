package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

import java.time.LocalDateTime;

@TableName("product_reviews")
public class ProductReviewPo extends BasePo {
    @TableId
    public Long id;
    public Long productId;
    public Long skuId;
    public String userNickname;
    public Integer rating;
    public String content;
    public String imageUrlsJson;
    public String tagsJson;
    public LocalDateTime reviewedAt;
    public String status;
    public Integer sortOrder;
}
