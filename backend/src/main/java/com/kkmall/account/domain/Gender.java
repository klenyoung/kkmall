package com.kkmall.account.domain;

/**
 * 性别枚举。
 */
public enum Gender {

    UNKNOWN,
    MALE,
    FEMALE;

    /**
     * 从字符串解析性别，空值或无效值返回 UNKNOWN。
     */
    public static Gender of(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
