package com.example.back.service;

import com.example.back.common.PageResult;
import com.example.back.dto.request.ArticleBatchDeleteRequest;
import com.example.back.dto.request.ArticleQueryRequest;
import com.example.back.dto.request.ArticleRequest;
import com.example.back.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章服务
 */
public interface ArticleService {

    ArticleResponse createArticle(ArticleRequest request);

    ArticleResponse updateArticle(Long articleId, ArticleRequest request);

    void deleteArticle(Long articleId);

    ArticleBatchDeleteResponse batchDeleteArticles(ArticleBatchDeleteRequest request);

    ArticleDetailResponse getArticleDetail(String identifier);

    PageResult<ArticleListItemResponse> getArticleList(ArticleQueryRequest request);

    PageResult<ArticleListItemResponse> getMyArticleList(ArticleQueryRequest request);

    ArticleImageUploadResponse uploadArticleImage(MultipartFile file);
}

