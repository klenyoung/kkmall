package com.kkmall.common.interfaces;

public final class ApiResponse<T> {
    public final Object code;
    public final String message;
    public final T data;
    public final String traceId;

    private ApiResponse(Object code, String message, T data, String traceId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data, null);
    }

    public static ApiResponse<Object> fail(Object code, String message, String traceId) {
        return new ApiResponse<>(code, message, null, traceId);
    }
}
