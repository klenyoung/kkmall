package com.kkmall.order.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

@TableName("addresses")
public class AddressPo extends BasePo {
    @TableId
    public Long id;
    public Long userId;
    public String receiverName;
    public String receiverPhone;
    public String region;
    public String detail;
    public Integer isDefault;
}
