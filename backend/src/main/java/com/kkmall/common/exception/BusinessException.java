package com.kkmall.common.exception;

import org.springframework.http.HttpStatus;

public final class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code) {
        this(code, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String code, HttpStatus status) {
        super(code);
        this.code = code;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }
}
