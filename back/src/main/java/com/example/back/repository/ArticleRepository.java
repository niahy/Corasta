package com.example.back.repository;

import com.example.back.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 文章仓库
 */
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    Optional<Article> findBySlug(String slug);

    Page<Article> findByUser_IdIn(List<Long> userIds, Pageable pageable);

    long countByUser_IdIn(List<Long> userIds);
}

