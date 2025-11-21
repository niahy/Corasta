package com.example.back.repository;

import com.example.back.entity.Notification;
import com.example.back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 通知仓库
 */
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    long countByUserAndReadIsFalse(User user);

    List<Notification> findByUserAndReadIsFalse(User user);
}

