package com.example.back.repository;

import com.example.back.entity.Answer;
import com.example.back.entity.AnswerVote;
import com.example.back.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 回答投票仓库
 */
public interface AnswerVoteRepository extends JpaRepository<AnswerVote, Long> {

    Optional<AnswerVote> findByAnswerAndUser(Answer answer, User user);

    @EntityGraph(attributePaths = {"answer"})
    List<AnswerVote> findByAnswer_IdInAndUser_Id(List<Long> answerIds, Long userId);
}

