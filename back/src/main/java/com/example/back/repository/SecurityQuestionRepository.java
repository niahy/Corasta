package com.example.back.repository;

import com.example.back.entity.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 密保问题仓库
 */
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    List<SecurityQuestion> findByUserIdOrderBySortOrderAsc(Long userId);
}

