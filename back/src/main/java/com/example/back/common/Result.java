package com.example.back.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一响应结果类
 *
 * @author Corasta Team
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null, LocalDateTime.now());
    }
    
    /**
     * 成功响应（有数据）
     */
    public static <T> Result<T> success(T data) {
        return success("操作成功", data);
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return of(200, message, data);
    }
    
    /**
     * 创建成功响应（201）
     */
    public static <T> Result<T> created(String message, T data) {
        return of(201, message, data);
    }
    
    /**
     * 自定义响应
     */
    public static <T> Result<T> of(Integer code, String message, T data) {
        return new Result<>(code, message, data, LocalDateTime.now());
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null, LocalDateTime.now());
    }
    
    /**
     * 失败响应（默认400错误码）
     */
    public static <T> Result<T> error(String message) {
        return error(400, message);
    }
}

