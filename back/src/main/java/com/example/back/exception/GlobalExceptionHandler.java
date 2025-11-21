package com.example.back.exception;

import com.example.back.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author Corasta Team
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Result<Object>> handleNotFoundException(NotFoundException e) {
        log.warn("资源不存在: {}", e.getMessage());
        Result<Object> result = Result.error(404, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Result<Object>> handleValidationException(ValidationException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        Result<Object> result = Result.error(422, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        Integer code = e.getCode() != null ? e.getCode() : 400;
        Result<Object> result = Result.error(code, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
    /**
     * 处理参数绑定验证异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Result<Map<String, String>> result = Result.error(422, "参数验证失败");
        result.setData(errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(result);
    }
    
    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Object>> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("未授权: {}", e.getMessage());
        Result<Object> result = Result.error(401, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
    
    /**
     * 处理禁止访问异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result<Object>> handleForbiddenException(ForbiddenException e) {
        log.warn("禁止访问: {}", e.getMessage());
        Result<Object> result = Result.error(403, e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }
    
    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.error("服务器内部错误", e);
        Result<Object> result = Result.error(500, "服务器内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}

