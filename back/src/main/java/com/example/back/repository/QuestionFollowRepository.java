package com.example.back.repository;

import com.example.back.entity.Question;
import com.example.back.entity.QuestionFollow;
import com.example.back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 问题关注仓库
 */
public interface QuestionFollowRepository extends JpaRepository<QuestionFollow, Long> {

    Optional<QuestionFollow> findByQuestionAndUser(Question question, User user);

    boolean existsByQuestionAndUser(Question question, User user);

    Long countByQuestion(Question question);
    
}

