package com.example.back.repository;

import com.example.back.entity.AdminOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 管理员操作日志仓库
 */
public interface AdminOperationLogRepository extends JpaRepository<AdminOperationLog, Long> {
}

