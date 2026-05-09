package com.kkmall.account.infrastructure;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kkmall.common.infrastructure.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户表持久化对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class UserPo extends BasePo {

    @TableId
    private Long id;

    private String phone;

    private String nickname;

    private String avatarUrl;

    private String gender;

    private LocalDate birthday;

    private String role;
}
