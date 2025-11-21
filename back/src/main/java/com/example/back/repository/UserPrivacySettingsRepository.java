package com.example.back.repository;

import com.example.back.entity.UserPrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户隐私设置仓库
 */
public interface UserPrivacySettingsRepository extends JpaRepository<UserPrivacySettings, Long> {

    Optional<UserPrivacySettings> findByUserId(Long userId);
}

