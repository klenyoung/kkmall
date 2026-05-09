package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商品评价表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_reviews")
public class ProductReviewPo extends BasePo {

    @TableId
    private Long id;

    private Long productId;

    private Long skuId;

    private String userNickname;

    private Integer rating;

    private String content;

    private String imageUrlsJson;

    private String tagsJson;

    private LocalDateTime reviewedAt;

    private String status;

    private Integer sortOrder;
}
