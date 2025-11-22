package com.example.back.controller;

import com.example.back.common.Result;
import com.example.back.dto.request.LoginRequest;
import com.example.back.dto.request.RegisterRequest;
import com.example.back.dto.response.CaptchaResponse;
import com.example.back.dto.response.LoginResponse;
import com.example.back.dto.response.RegisterResponse;
import com.example.back.dto.response.SecurityQuestionResponse;
import com.example.back.service.AuthService;
import com.example.back.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证相关接口
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result<CaptchaResponse> getCaptcha() {
        CaptchaResponse response = captchaService.generateCaptcha();
        return Result.success(response);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return Result.created("注册成功", response);
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    @GetMapping("/security-questions/{username}")
    public Result<List<SecurityQuestionResponse>> getSecurityQuestions(@PathVariable String username) {
        List<SecurityQuestionResponse> responses = authService.getSecurityQuestions(username);
        return Result.success(responses);
    }
}

