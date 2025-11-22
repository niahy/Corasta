package com.example.back.repository;

import com.example.back.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文章仓库
 */
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Article> findAllById(Iterable<Long> ids);

    @Override
    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Optional<Article> findById(Long id);

    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Optional<Article> findBySlug(String slug);

    @Override
    @EntityGraph(attributePaths = {"user", "category", "tags"})
    Page<Article> findAll(org.springframework.data.jpa.domain.Specification<Article> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Article> findByUser_IdIn(List<Long> userIds, Pageable pageable);

    long countByUser_IdIn(List<Long> userIds);

    long countByUser_Id(Long userId);

    long countByUser_IdAndStatus(Long userId, Integer status);

    List<Article> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("select coalesce(sum(a.viewCount), 0) from Article a where a.user.id = :userId")
    long sumViewCountByUser(Long userId);

    @Query("select coalesce(sum(a.likeCount), 0) from Article a where a.user.id = :userId")
    long sumLikeCountByUser(Long userId);

    @Query("select coalesce(sum(a.commentCount), 0) from Article a where a.user.id = :userId")
    long sumCommentCountByUser(Long userId);

    @Query("select coalesce(sum(a.favoriteCount), 0) from Article a where a.user.id = :userId")
    long sumFavoriteCountByUser(Long userId);

    @Query("select function('date', a.createdAt), count(a) from Article a where a.user.id = :userId and a.createdAt between :start and :end group by function('date', a.createdAt) order by function('date', a.createdAt)")
    List<Object[]> countDailyArticles(Long userId, LocalDateTime start, LocalDateTime end);
}

