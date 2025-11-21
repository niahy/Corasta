package com.example.back.repository;

import com.example.back.entity.Favorite;
import com.example.back.entity.FavoriteFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 收藏仓库
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {

    Optional<Favorite> findByUser_IdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    Long countByFolder(FavoriteFolder folder);

    Page<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}

