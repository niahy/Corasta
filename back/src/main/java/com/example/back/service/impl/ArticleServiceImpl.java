package com.example.back.service.impl;

import com.example.back.common.Constants;
import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.ArticleBatchDeleteRequest;
import com.example.back.dto.request.ArticleQueryRequest;
import com.example.back.dto.request.ArticleRequest;
import com.example.back.dto.response.*;
import com.example.back.entity.Article;
import com.example.back.entity.Category;
import com.example.back.entity.Tag;
import com.example.back.entity.User;
import com.example.back.exception.ForbiddenException;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.ArticleRepository;
import com.example.back.repository.CategoryRepository;
import com.example.back.repository.TagRepository;
import com.example.back.repository.UserRepository;
import com.example.back.service.ArticleService;
import com.example.back.storage.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));

        ensureSlugUnique(request.getSlug(), null);

        Article article = Article.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .coverImage(request.getCoverImage())
                .slug(normalizeSlug(request.getSlug()))
                .status(request.getStatus())
                .category(fetchCategory(request.getCategoryId()))
                .publishedAt(resolvePublishedAt(request.getStatus(), null))
                .build();
        article.setTags(resolveTags(request.getTags()));

        Article saved = articleRepository.save(article);
        return toArticleResponse(saved);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(Long articleId, ArticleRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException("文章不存在"));
        ensureOwner(userId, article);

        ensureSlugUnique(request.getSlug(), article.getId());

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setSlug(normalizeSlug(request.getSlug()));
        article.setStatus(request.getStatus());
        article.setCategory(fetchCategory(request.getCategoryId()));
        article.setPublishedAt(resolvePublishedAt(request.getStatus(), article.getPublishedAt()));

        Set<Tag> newTags = resolveTags(request.getTags());
        article.getTags().clear();
        article.getTags().addAll(newTags);

        return toArticleResponse(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long articleId) {
        Long userId = AuthContextHolder.requireUserId();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException("文章不存在"));
        ensureOwner(userId, article);
        article.setDeletedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public ArticleBatchDeleteResponse batchDeleteArticles(ArticleBatchDeleteRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        List<Article> articles = articleRepository.findAllById(request.getIds()).stream()
                .filter(article -> article.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        if (articles.isEmpty()) {
            throw new ValidationException("没有可删除的文章");
        }
        LocalDateTime now = LocalDateTime.now();
        articles.forEach(article -> article.setDeletedAt(now));
        return new ArticleBatchDeleteResponse(articles.size());
    }

    @Override
    @Transactional
    public ArticleDetailResponse getArticleDetail(String identifier) {
        Article article = findArticleByIdentifier(identifier);
        ensureArticleVisible(article);
        article.setViewCount(article.getViewCount() + 1);
        return toArticleDetailResponse(article, false, false);
    }

    @Override
    public PageResult<ArticleListItemResponse> getArticleList(ArticleQueryRequest request) {
        Pageable pageable = buildPageable(request);
        Specification<Article> specification = buildSpecification(request, false);
        Page<Article> page = articleRepository.findAll(specification, pageable);
        List<ArticleListItemResponse> items = page.stream()
                .map(this::toArticleListItemResponse)
                .collect(Collectors.toList());
        return PageResult.of(items, pageable.getPageNumber() + 1, pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    public PageResult<ArticleListItemResponse> getMyArticleList(ArticleQueryRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        request.setUserId(userId);
        Pageable pageable = buildPageable(request);
        Specification<Article> specification = buildSpecification(request, true);
        Page<Article> page = articleRepository.findAll(specification, pageable);
        List<ArticleListItemResponse> items = page.stream()
                .map(this::toArticleListItemResponse)
                .collect(Collectors.toList());
        return PageResult.of(items, pageable.getPageNumber() + 1, pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    public ArticleImageUploadResponse uploadArticleImage(MultipartFile file) {
        Long userId = AuthContextHolder.requireUserId();
        String url = fileStorageService.storeArticleImage(userId, file);
        return new ArticleImageUploadResponse(url);
    }

    private void ensureSlugUnique(String slug, Long currentId) {
        if (!StringUtils.hasText(slug)) {
            return;
        }
        articleRepository.findBySlug(slug.trim())
                .filter(article -> !article.getId().equals(currentId))
                .ifPresent(article -> {
                    throw new ValidationException("Slug已被占用");
                });
    }

    private String normalizeSlug(String slug) {
        return StringUtils.hasText(slug) ? slug.trim() : null;
    }

    private Category fetchCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ValidationException("分类不存在"));
    }

    private LocalDateTime resolvePublishedAt(Integer status, LocalDateTime originalPublishedAt) {
        if (status == null) {
            return originalPublishedAt;
        }
        if (status.equals(Constants.STATUS_PUBLISHED)) {
            return originalPublishedAt != null ? originalPublishedAt : LocalDateTime.now();
        }
        return null;
    }

    private Set<Tag> resolveTags(List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return new HashSet<>();
        }
        List<String> normalizedNames = tagNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(name -> name.length() > 50 ? name.substring(0, 50) : name)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        if (normalizedNames.isEmpty()) {
            return new HashSet<>();
        }
        Map<String, Tag> existing = tagRepository.findByNameIn(normalizedNames).stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));
        Set<Tag> result = new LinkedHashSet<>();
        for (String name : normalizedNames) {
            Tag tag = existing.get(name);
            if (tag == null) {
                tag = Tag.builder().name(name).useCount(0).build();
            }
            result.add(tag);
        }
        return result;
    }

    private void ensureOwner(Long userId, Article article) {
        if (!article.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权操作该文章");
        }
    }

    private Article findArticleByIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            throw new ValidationException("文章标识不能为空");
        }
        try {
            Long id = Long.parseLong(identifier);
            Article article = articleRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("文章不存在"));
            if (article.getDeletedAt() != null) {
                throw new NotFoundException("文章不存在");
            }
            return article;
        } catch (NumberFormatException ex) {
            Article article = articleRepository.findBySlug(identifier)
                    .orElseThrow(() -> new NotFoundException("文章不存在"));
            if (article.getDeletedAt() != null) {
                throw new NotFoundException("文章不存在");
            }
            return article;
        }
    }

    private void ensureArticleVisible(Article article) {
        Long currentUserId = AuthContextHolder.getCurrentUser()
                .map(authUser -> authUser.getId())
                .orElse(null);
        boolean isOwner = currentUserId != null && currentUserId.equals(article.getUser().getId());
        if (!isOwner && !article.getStatus().equals(Constants.STATUS_PUBLISHED)) {
            throw new ForbiddenException("文章未公开");
        }
    }

    private Specification<Article> buildSpecification(ArticleQueryRequest request, boolean onlySelf) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            Long currentUserId = AuthContextHolder.getCurrentUser()
                    .map(authUser -> authUser.getId())
                    .orElse(null);
            boolean allowStatusFilter = onlySelf;

            if (onlySelf) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            } else {
                if (request.getUserId() != null) {
                    predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
                    if (currentUserId == null || !request.getUserId().equals(currentUserId)) {
                        predicates.add(cb.equal(root.get("status"), Constants.STATUS_PUBLISHED));
                    } else {
                        allowStatusFilter = true;
                    }
                } else {
                    predicates.add(cb.equal(root.get("status"), Constants.STATUS_PUBLISHED));
                }
            }

            if (allowStatusFilter && request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            String keyword = request.normalizedKeyword();
            if (keyword != null) {
                String likePattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likePattern),
                        cb.like(root.get("summary"), likePattern)
                ));
            }
            if (request.getTagId() != null) {
                Join<Article, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(cb.equal(tagJoin.get("id"), request.getTagId()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private Pageable buildPageable(ArticleQueryRequest request) {
        int page = Optional.ofNullable(request.getPage()).orElse(1) - 1;
        int size = Optional.ofNullable(request.getPageSize()).orElse(Constants.DEFAULT_PAGE_SIZE);
        String sort = request.normalizedSort();
        Sort sortOption;
        switch (sort) {
            case "popular" -> sortOption = Sort.by(Sort.Order.desc("likeCount"));
            case "views" -> sortOption = Sort.by(Sort.Order.desc("viewCount"));
            default -> sortOption = Sort.by(Sort.Order.desc("createdAt"));
        }
        return PageRequest.of(Math.max(page, 0), size, sortOption);
    }

    private ArticleResponse toArticleResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .slug(article.getSlug())
                .status(article.getStatus())
                .createdAt(article.getCreatedAt())
                .build();
    }

    private ArticleDetailResponse toArticleDetailResponse(Article article, boolean liked, boolean favorited) {
        return ArticleDetailResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .coverImage(article.getCoverImage())
                .slug(article.getSlug())
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .favoriteCount(article.getFavoriteCount())
                .liked(liked)
                .favorited(favorited)
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .author(new ArticleDetailResponse.AuthorInfo(
                        article.getUser().getId(),
                        article.getUser().getUsername(),
                        article.getUser().getNickname(),
                        article.getUser().getAvatar()
                ))
                .category(article.getCategory() == null ? null :
                        new ArticleDetailResponse.CategoryInfo(article.getCategory().getId(), article.getCategory().getName()))
                .tags(article.getTags().stream()
                        .map(tag -> new ArticleDetailResponse.TagInfo(tag.getId(), tag.getName()))
                        .collect(Collectors.toList()))
                .build();
    }

    private ArticleListItemResponse toArticleListItemResponse(Article article) {
        return ArticleListItemResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .coverImage(article.getCoverImage())
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .createdAt(article.getCreatedAt())
                .author(new ArticleListItemResponse.AuthorInfo(
                        article.getUser().getId(),
                        article.getUser().getNickname(),
                        article.getUser().getAvatar()
                ))
                .category(article.getCategory() == null ? null :
                        new ArticleListItemResponse.CategoryInfo(article.getCategory().getId(), article.getCategory().getName()))
                .tags(article.getTags().stream()
                        .map(tag -> new ArticleListItemResponse.TagInfo(tag.getId(), tag.getName()))
                        .collect(Collectors.toList()))
                .build();
    }
}

