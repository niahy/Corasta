package com.example.back.controller;

import com.example.back.common.PageResult;
import com.example.back.common.Result;
import com.example.back.dto.request.ArticleBatchDeleteRequest;
import com.example.back.dto.request.ArticleQueryRequest;
import com.example.back.dto.request.ArticleRequest;
import com.example.back.dto.response.*;
import com.example.back.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章接口
 */
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping
    public Result<ArticleResponse> createArticle(@Valid @RequestBody ArticleRequest request) {
        ArticleResponse response = articleService.createArticle(request);
        return Result.created("创建成功", response);
    }

    @PutMapping("/{articleId}")
    public Result<ArticleResponse> updateArticle(@PathVariable Long articleId,
                                                 @Valid @RequestBody ArticleRequest request) {
        ArticleResponse response = articleService.updateArticle(articleId, request);
        return Result.success("更新成功", response);
    }

    @DeleteMapping("/{articleId}")
    public Result<Void> deleteArticle(@PathVariable Long articleId) {
        articleService.deleteArticle(articleId);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    public Result<ArticleBatchDeleteResponse> batchDelete(@Valid @RequestBody ArticleBatchDeleteRequest request) {
        ArticleBatchDeleteResponse response = articleService.batchDeleteArticles(request);
        return Result.success("删除成功", response);
    }

    @GetMapping
    public Result<PageResult<ArticleListItemResponse>> getArticles(@Valid ArticleQueryRequest request) {
        PageResult<ArticleListItemResponse> result = articleService.getArticleList(request);
        return Result.success(result);
    }

    @GetMapping("/me")
    public Result<PageResult<ArticleListItemResponse>> getMyArticles(@Valid ArticleQueryRequest request) {
        PageResult<ArticleListItemResponse> result = articleService.getMyArticleList(request);
        return Result.success(result);
    }

    @GetMapping("/{identifier}")
    public Result<ArticleDetailResponse> getArticleDetail(@PathVariable String identifier) {
        ArticleDetailResponse response = articleService.getArticleDetail(identifier);
        return Result.success(response);
    }

    @PostMapping("/images")
    public Result<ArticleImageUploadResponse> uploadArticleImage(@RequestParam("file") MultipartFile file) {
        ArticleImageUploadResponse response = articleService.uploadArticleImage(file);
        return Result.success(response);
    }
}

