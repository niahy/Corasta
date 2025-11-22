package com.example.back.repository;

import com.example.back.entity.Notification;
import com.example.back.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 通知仓库
 */
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    long countByUserAndReadIsFalse(User user);

    List<Notification> findByUserAndReadIsFalse(User user);

    @EntityGraph(attributePaths = {"sender"})
    Page<Notification> findAll(Specification<Notification> spec, Pageable pageable);
}

