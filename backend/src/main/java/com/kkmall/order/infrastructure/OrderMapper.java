package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface OrderMapper extends BaseMapper<OrderPo> {

    /**
     * 原子性地将订单状态从 expectedStatus 更新为 newStatus，防止并发重复支付。
     *
     * @return 受影响行数，1 表示成功，0 表示状态已变更（并发竞争失败）
     */
    @Update("UPDATE orders SET status = #{newStatus}, paid_at = #{paidAt}, updated_at = NOW() "
            + "WHERE id = #{orderId} AND status = #{expectedStatus} AND deleted = 0")
    int compareAndUpdateStatus(@Param("orderId") Long orderId,
                               @Param("expectedStatus") String expectedStatus,
                               @Param("newStatus") String newStatus,
                               @Param("paidAt") LocalDateTime paidAt);
}
