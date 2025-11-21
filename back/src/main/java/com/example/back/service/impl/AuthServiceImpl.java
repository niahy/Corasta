package com.example.back.service.impl;

import com.example.back.common.Constants;
import com.example.back.dto.request.LoginRequest;
import com.example.back.dto.request.RegisterRequest;
import com.example.back.dto.response.LoginResponse;
import com.example.back.dto.response.RegisterResponse;
import com.example.back.dto.response.SecurityQuestionResponse;
import com.example.back.entity.SecurityQuestion;
import com.example.back.entity.User;
import com.example.back.exception.BusinessException;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.SecurityQuestionRepository;
import com.example.back.repository.UserRepository;
import com.example.back.service.AuthService;
import com.example.back.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final SecurityQuestionRepository securityQuestionRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("两次输入的密码不一致");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .nickname("Corasta用户")
                .status(Constants.USER_STATUS_NORMAL)
                .role(Constants.USER_ROLE_NORMAL)
                .build();
        User savedUser = userRepository.save(user);

        List<SecurityQuestion> toSave = new ArrayList<>();
        if (!CollectionUtils.isEmpty(request.getSecurityQuestions())) {
            for (RegisterRequest.SecurityQuestionRequest sqReq : request.getSecurityQuestions()) {
                SecurityQuestion question = SecurityQuestion.builder()
                        .user(savedUser)
                        .question(sqReq.getQuestion())
                        .answer(sqReq.getAnswer())
                        .sortOrder(sqReq.getSortOrder())
                        .build();
                toSave.add(question);
            }
            securityQuestionRepository.saveAll(toSave);
        }

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ValidationException("用户名或密码错误");
        }

        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(), rememberMe);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar()
        );
        return new LoginResponse(token, userInfo);
    }

    @Override
    public List<SecurityQuestionResponse> getSecurityQuestions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        List<SecurityQuestion> questions = securityQuestionRepository.findByUserIdOrderBySortOrderAsc(user.getId());
        return questions.stream()
                .map(q -> new SecurityQuestionResponse(q.getId(), q.getQuestion(), q.getSortOrder()))
                .collect(Collectors.toList());
    }
}

