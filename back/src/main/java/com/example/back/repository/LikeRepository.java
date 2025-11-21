package com.example.back.repository;

import com.example.back.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 点赞仓库
 */
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByUser_IdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    List<Like> findByUser_IdAndTargetTypeAndTargetIdIn(Long userId, String targetType, List<Long> targetIds);

    Long countByTargetTypeAndTargetId(String targetType, Long targetId);

    void deleteByUser_IdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);

    Page<Like> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}

