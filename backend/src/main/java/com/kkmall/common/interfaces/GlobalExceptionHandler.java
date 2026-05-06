package com.kkmall.common.interfaces;

import com.kkmall.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> business(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.status()).body(ApiResponse.fail(ex.code(), ex.code(), traceId(request)));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ApiResponse<Object>> badRequest(Exception ex, HttpServletRequest request) {
        String message = ex.getMessage() == null ? "BAD_REQUEST" : ex.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("BAD_REQUEST", message, traceId(request)));
    }

    private String traceId(HttpServletRequest request) {
        Object value = request.getAttribute("traceId");
        return value == null ? null : value.toString();
    }
}
