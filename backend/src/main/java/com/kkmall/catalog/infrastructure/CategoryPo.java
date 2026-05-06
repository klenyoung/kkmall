package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("categories")
public class CategoryPo extends BasePo {
    @TableId
    public Long id;
    public String name;
    public Integer sortOrder;
    public Integer enabled;
}
