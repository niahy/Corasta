package com.example.back.repository;

import com.example.back.entity.SearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索日志仓库
 */
public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

    // 使用 GROUP BY 替代 DISTINCT，这样可以在 ORDER BY 中使用 created_at
    @Query("select l.keyword from SearchLog l where lower(l.keyword) like lower(concat(:keyword, '%')) group by l.keyword order by max(l.createdAt) desc")
    List<String> findSuggestions(String keyword, Pageable pageable);

    @Query("select l.keyword, count(l) from SearchLog l where l.createdAt >= :start group by l.keyword order by count(l) desc")
    List<Object[]> findHotKeywords(LocalDateTime start, Pageable pageable);

    @Query("select l.keyword from SearchLog l where l.user.id = :userId order by l.createdAt desc")
    List<String> findHistoryByUser(Long userId, Pageable pageable);

    void deleteByUser_Id(Long userId);
}

