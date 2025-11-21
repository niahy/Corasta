package com.example.back.service;

import com.example.back.dto.request.UpdatePrivacySettingsRequest;
import com.example.back.dto.request.UpdateProfileRequest;
import com.example.back.dto.response.AvatarUploadResponse;
import com.example.back.dto.response.CurrentUserResponse;
import com.example.back.dto.response.UserHomeResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务
 */
public interface UserService {

    CurrentUserResponse getCurrentUserProfile();

    CurrentUserResponse updateProfile(UpdateProfileRequest request);

    void updatePrivacySettings(UpdatePrivacySettingsRequest request);

    UserHomeResponse getUserHome(Long userId);

    AvatarUploadResponse uploadAvatar(MultipartFile file);
}

