package com.kkmall.catalog.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SkuMapper extends BaseMapper<SkuPo> {
    @Update("UPDATE skus SET stock = stock - #{quantity} WHERE id = #{skuId} AND stock >= #{quantity} AND deleted = 0")
    int decreaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
