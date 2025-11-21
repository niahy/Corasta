package com.example.back.exception;

/**
 * 业务异常基类
 *
 * @author Corasta Team
 * @since 1.0
 */
public class BusinessException extends RuntimeException {
    
    private Integer code;
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
}

