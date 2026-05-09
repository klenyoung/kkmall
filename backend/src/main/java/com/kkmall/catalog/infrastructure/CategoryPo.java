package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分类表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("categories")
public class CategoryPo extends BasePo {

    @TableId
    private Long id;

    private String name;

    private Integer sortOrder;

    private Integer enabled;
}
