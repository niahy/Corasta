package com.example.back.repository;

import com.example.back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓库
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Query("select function('date', u.createdAt), count(u) from User u where u.createdAt between :start and :end group by function('date', u.createdAt) order by function('date', u.createdAt)")
    List<Object[]> countDailyUsers(LocalDateTime start, LocalDateTime end);
}

