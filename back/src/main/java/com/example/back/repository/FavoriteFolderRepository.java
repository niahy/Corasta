package com.example.back.repository;

import com.example.back.entity.FavoriteFolder;
import com.example.back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹仓库
 */
public interface FavoriteFolderRepository extends JpaRepository<FavoriteFolder, Long> {

    List<FavoriteFolder> findByUserOrderBySortOrderAscIdAsc(User user);

    Optional<FavoriteFolder> findByIdAndUser(Long id, User user);
}

