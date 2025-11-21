package com.example.back.controller;

import com.example.back.common.Result;
import com.example.back.dto.request.UpdatePrivacySettingsRequest;
import com.example.back.dto.request.UpdateProfileRequest;
import com.example.back.dto.response.AvatarUploadResponse;
import com.example.back.dto.response.CurrentUserResponse;
import com.example.back.dto.response.UserHomeResponse;
import com.example.back.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<CurrentUserResponse> getCurrentUser() {
        return Result.success(userService.getCurrentUserProfile());
    }

    @PutMapping("/me")
    public Result<CurrentUserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return Result.success("更新成功", userService.updateProfile(request));
    }

    @PutMapping("/me/privacy")
    public Result<Void> updatePrivacy(@Valid @RequestBody UpdatePrivacySettingsRequest request) {
        userService.updatePrivacySettings(request);
        return Result.success("更新成功", null);
    }

    @GetMapping("/{userId}")
    public Result<UserHomeResponse> getUserHome(@PathVariable Long userId) {
        return Result.success(userService.getUserHome(userId));
    }

    @PostMapping("/me/avatar")
    public Result<AvatarUploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success(userService.uploadAvatar(file));
    }
}

