package com.example.back.repository;

import com.example.back.entity.Answer;
import com.example.back.entity.Question;
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
 * 回答仓库
 */
public interface AnswerRepository extends JpaRepository<Answer, Long>, JpaSpecificationExecutor<Answer> {

    List<Answer> findByQuestion(Question question);

    long countByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"user", "question", "question.user"})
    Page<Answer> findByUser_Id(Long userId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "question", "question.user"})
    List<Answer> findAllById(Iterable<Long> ids);

    @Override
    @EntityGraph(attributePaths = {"user", "question", "question.user"})
    Page<Answer> findAll(org.springframework.data.jpa.domain.Specification<Answer> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "question", "question.user"})
    Optional<Answer> findById(Long id);

    @EntityGraph(attributePaths = {"user", "question", "question.user"})
    List<Answer> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("select coalesce(sum(a.upvoteCount), 0) from Answer a where a.user.id = :userId")
    long sumUpvoteCountByUser(Long userId);

    @Query("select coalesce(sum(a.commentCount), 0) from Answer a where a.user.id = :userId")
    long sumCommentCountByUser(Long userId);

    @Query("select function('date', a.createdAt), count(a) from Answer a where a.user.id = :userId and a.createdAt between :start and :end group by function('date', a.createdAt) order by function('date', a.createdAt)")
    List<Object[]> countDailyAnswers(Long userId, LocalDateTime start, LocalDateTime end);
}

