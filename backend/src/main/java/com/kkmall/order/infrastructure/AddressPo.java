package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("addresses")
public class AddressPo extends BasePo {

    @TableId
    private Long id;

    private Long userId;

    private String receiverName;

    private String receiverPhone;

    private String region;

    private String detail;

    private Integer isDefault;
}
