package com.example.back.service;

import com.example.back.dto.request.LoginRequest;
import com.example.back.dto.request.RegisterRequest;
import com.example.back.dto.response.LoginResponse;
import com.example.back.dto.response.RegisterResponse;
import com.example.back.dto.response.SecurityQuestionResponse;

import java.util.List;

/**
 * 认证相关服务
 */
public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    List<SecurityQuestionResponse> getSecurityQuestions(String username);
}

