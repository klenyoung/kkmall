package com.kkmall.account.domain;

public final class PhoneNumber {
    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String value) {
        if (value == null || !value.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("AUTH_INVALID_PHONE");
        }
        return new PhoneNumber(value);
    }

    public String value() {
        return value;
    }
}
