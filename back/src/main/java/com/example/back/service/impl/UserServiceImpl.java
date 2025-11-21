package com.example.back.service.impl;

import com.example.back.common.Constants;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.UpdatePrivacySettingsRequest;
import com.example.back.dto.request.UpdateProfileRequest;
import com.example.back.dto.response.AvatarUploadResponse;
import com.example.back.dto.response.CurrentUserResponse;
import com.example.back.dto.response.UserHomeResponse;
import com.example.back.entity.User;
import com.example.back.entity.UserPrivacySettings;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.UserPrivacySettingsRepository;
import com.example.back.repository.UserRepository;
import com.example.back.service.UserService;
import com.example.back.storage.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务实现
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPrivacySettingsRepository privacySettingsRepository;
    private final FileStorageService fileStorageService;

    @Override
    public CurrentUserResponse getCurrentUserProfile() {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));

        return buildCurrentUserResponse(user);
    }

    @Override
    @Transactional
    public CurrentUserResponse updateProfile(UpdateProfileRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));

        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        return buildCurrentUserResponse(user);
    }

    @Override
    @Transactional
    public void updatePrivacySettings(UpdatePrivacySettingsRequest request) {
        Long userId = AuthContextHolder.requireUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));

        UserPrivacySettings settings = privacySettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> UserPrivacySettings.builder()
                        .user(user)
                        .homepageVisible(Constants.USER_STATUS_NORMAL)
                        .contentVisible(Constants.USER_STATUS_NORMAL)
                        .build());

        settings.setHomepageVisible(request.getHomepageVisible());
        settings.setContentVisible(request.getContentVisible());
        privacySettingsRepository.save(settings);
    }

    @Override
    public UserHomeResponse getUserHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));

        UserHomeResponse.UserInfo userInfo = new UserHomeResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getBio(),
                0,
                0,
                0
        );

        UserHomeResponse.ContentOverview overview = new UserHomeResponse.ContentOverview(
                0, 0, 0
        );

        boolean following = false;

        return UserHomeResponse.builder()
                .user(userInfo)
                .following(following)
                .content(overview)
                .build();
    }

    @Override
    @Transactional
    public AvatarUploadResponse uploadAvatar(MultipartFile file) {
        Long userId = AuthContextHolder.requireUserId();
        if (file == null || file.isEmpty()) {
            throw new ValidationException("请选择需要上传的文件");
        }
        String url = fileStorageService.storeAvatar(userId, file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        user.setAvatar(url);
        return new AvatarUploadResponse(url);
    }

    private CurrentUserResponse buildCurrentUserResponse(User user) {
        return CurrentUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .followerCount(0)
                .followingCount(0)
                .likeCount(0)
                .articleCount(0)
                .questionCount(0)
                .answerCount(0)
                .build();
    }
}

