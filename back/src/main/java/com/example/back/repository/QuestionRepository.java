package com.example.back.repository;

import com.example.back.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题仓库
 */
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    @EntityGraph(attributePaths = {"user"})
    Page<Question> findByUser_IdIn(List<Long> userIds, Pageable pageable);

    long countByUser_IdIn(List<Long> userIds);

    long countByUser_Id(Long userId);

    List<Question> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);

    List<Question> findTop5ByUser_IdAndAnswerCountOrderByCreatedAtAsc(Long userId, Integer answerCount);

    @Query("select coalesce(sum(q.viewCount), 0) from Question q where q.user.id = :userId")
    long sumViewCountByUser(Long userId);

    @Query("select function('date', q.createdAt), count(q) from Question q where q.user.id = :userId and q.createdAt between :start and :end group by function('date', q.createdAt) order by function('date', q.createdAt)")
    List<Object[]> countDailyQuestions(Long userId, LocalDateTime start, LocalDateTime end);
}

