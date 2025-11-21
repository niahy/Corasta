package com.example.back.repository;

import com.example.back.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 问题仓库
 */
public interface QuestionRepository extends JpaRepository<Question, Long>, JpaSpecificationExecutor<Question> {

    Page<Question> findByUser_IdIn(List<Long> userIds, Pageable pageable);

    long countByUser_IdIn(List<Long> userIds);
}

