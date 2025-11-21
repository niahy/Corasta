package com.example.back.repository;

import com.example.back.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 评论仓库
 */
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    List<Comment> findByParentIdIn(List<Long> parentIds);

    Long countByParentId(Long parentId);

    List<Comment> findByTargetTypeAndTargetIdAndPinned(String targetType, Long targetId, Integer pinned);

    Page<Comment> findByUser_Id(Long userId, Pageable pageable);
}

