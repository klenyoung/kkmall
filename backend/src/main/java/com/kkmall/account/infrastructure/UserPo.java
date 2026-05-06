package com.kkmall.account.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;

import java.time.LocalDate;

@TableName("users")
public class UserPo extends BasePo {
    @TableId
    public Long id;
    public String phone;
    public String nickname;
    public String avatarUrl;
    public String gender;
    public LocalDate birthday;
    public String role;
}
