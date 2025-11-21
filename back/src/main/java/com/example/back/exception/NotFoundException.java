package com.example.back.exception;

/**
 * 资源不存在异常
 *
 * @author Corasta Team
 * @since 1.0
 */
public class NotFoundException extends RuntimeException {
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

