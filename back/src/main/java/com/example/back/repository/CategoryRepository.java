package com.example.back.repository;

import com.example.back.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 分类仓库
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}

