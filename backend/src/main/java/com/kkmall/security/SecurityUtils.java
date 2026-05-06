package com.kkmall.security;

import com.kkmall.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static AuthUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser)) {
            throw new BusinessException("AUTH_REQUIRED", HttpStatus.UNAUTHORIZED);
        }
        return (AuthUser) authentication.getPrincipal();
    }

    public static Long currentUserId() {
        return currentUser().id();
    }
}
