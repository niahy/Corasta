package com.example.back.repository;

import com.example.back.entity.Follow;
import com.example.back.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 关注关系仓库
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    Page<Follow> findByFollower(User follower, Pageable pageable);

    Page<Follow> findByFollowing(User following, Pageable pageable);

    long countByFollower(User follower);

    long countByFollowing(User following);

    @Query("select f.following.id from Follow f where f.follower.id = :followerId")
    List<Long> findFollowingIdsByFollowerId(Long followerId);

    @Query("select f.following.id, count(f) from Follow f where f.following.id in :userIds group by f.following.id")
    List<Object[]> countFollowersByUserIds(List<Long> userIds);

    @Query("select f.follower.id, count(f) from Follow f where f.follower.id in :userIds group by f.follower.id")
    List<Object[]> countFollowingByUserIds(List<Long> userIds);

    @Query("select f.following.id from Follow f where f.follower.id = :followerId and f.following.id in :targetIds")
    List<Long> findFollowingIds(Long followerId, List<Long> targetIds);

    long countByFollowing_Id(Long userId);

    @Query("select function('date', f.createdAt), count(f) from Follow f where f.following.id = :userId and f.createdAt between :start and :end group by function('date', f.createdAt) order by function('date', f.createdAt)")
    List<Object[]> countDailyFollowers(Long userId, LocalDateTime start, LocalDateTime end);
}

