package com.example.back.exception;

/**
 * 禁止访问异常
 *
 * @author Corasta Team
 * @since 1.0
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

