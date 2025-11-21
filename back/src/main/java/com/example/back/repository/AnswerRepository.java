package com.example.back.repository;

import com.example.back.entity.Answer;
import com.example.back.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 回答仓库
 */
public interface AnswerRepository extends JpaRepository<Answer, Long>, JpaSpecificationExecutor<Answer> {

    List<Answer> findByQuestion(Question question);
}

