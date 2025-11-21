package com.example.back.exception;

/**
 * 未授权异常
 *
 * @author Corasta Team
 * @since 1.0
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

