package com.example.back.exception;

/**
 * 参数验证异常
 *
 * @author Corasta Team
 * @since 1.0
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

