package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.FavoriteFolderRequest;
import com.example.back.dto.request.FavoriteQueryRequest;
import com.example.back.dto.request.FavoriteRequest;
import com.example.back.dto.response.FavoriteFolderResponse;
import com.example.back.dto.response.FavoriteItemResponse;

import java.util.List;

/**
 * 收藏服务
 */
public interface FavoriteService {

    FavoriteFolderResponse createFolder(FavoriteFolderRequest request);

    List<FavoriteFolderResponse> listFolders();

    void deleteFolder(Long folderId);

    void updateFolderOrder(Long folderId, Integer sortOrder);

    void favorite(FavoriteRequest request);

    void cancel(String targetType, Long targetId);

    PageResult<FavoriteItemResponse> listFavorites(FavoriteQueryRequest request);
}

