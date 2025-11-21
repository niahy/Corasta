package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.FavoriteFolderRequest;
import com.example.back.dto.request.FavoriteQueryRequest;
import com.example.back.dto.request.FavoriteRequest;
import com.example.back.dto.response.FavoriteFolderResponse;
import com.example.back.dto.response.FavoriteItemResponse;
import com.example.back.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/favorites/folders")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<FavoriteFolderResponse> createFolder(@Valid @RequestBody FavoriteFolderRequest request) {
        return Result.created("创建成功", favoriteService.createFolder(request));
    }

    @GetMapping("/favorites/folders")
    public Result<List<FavoriteFolderResponse>> listFolders() {
        return Result.success(favoriteService.listFolders());
    }

    @PostMapping("/favorites")
    public Result<Void> favorite(@Valid @RequestBody FavoriteRequest request) {
        favoriteService.favorite(request);
        return Result.success("收藏成功", null);
    }

    @DeleteMapping("/favorites")
    public Result<Void> cancelFavorite(@RequestParam String targetType,
                                       @RequestParam Long targetId) {
        favoriteService.cancel(targetType, targetId);
        return Result.success("取消收藏成功", null);
    }

    @GetMapping("/favorites")
    public Result<PageResult<FavoriteItemResponse>> listFavorites(@Valid FavoriteQueryRequest request) {
        return Result.success(favoriteService.listFavorites(request));
    }
}

