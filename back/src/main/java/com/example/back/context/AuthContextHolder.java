package com.example.back.context;

import com.example.back.exception.UnauthorizedException;

import java.util.Optional;

/**
 * 认证上下文持有者
 */
public final class AuthContextHolder {

    private static final ThreadLocal<AuthUser> CONTEXT = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthUser user) {
        CONTEXT.set(user);
    }

    public static Optional<AuthUser> getCurrentUser() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static AuthUser requireUser() {
        return getCurrentUser().orElseThrow(() -> new UnauthorizedException("请先登录"));
    }

    public static Long requireUserId() {
        return requireUser().getId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

