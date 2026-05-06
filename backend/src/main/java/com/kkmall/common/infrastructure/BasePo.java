package com.kkmall.common.infrastructure;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import java.time.LocalDateTime;

public abstract class BasePo {
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    public LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    public LocalDateTime updatedAt;

    @TableLogic
    public Integer deleted;
}
