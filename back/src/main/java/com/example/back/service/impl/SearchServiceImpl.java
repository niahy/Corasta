package com.example.back.service.impl;

import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.SearchRequest;
import com.example.back.dto.response.*;
import com.example.back.entity.*;
import com.example.back.exception.ValidationException;
import com.example.back.repository.*;
import com.example.back.service.SearchService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int ALL_MODE_LIMIT = 5;
    private static final int SUGGESTION_LIMIT = 8;
    private static final int HOT_LIMIT = 10;
    private static final int HISTORY_LIMIT = 10;

    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final SearchLogRepository searchLogRepository;

    @Override
    public SearchResponse search(SearchRequest request) {
        String keyword = Optional.ofNullable(request.normalizedKeyword())
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new ValidationException("搜索关键词不能为空"));
        String type = request.normalizedType();

        SearchResponse.Section<SearchResponse.ArticleItem> articleSection =
                new SearchResponse.Section<>(Collections.emptyList(), 0L);
        SearchResponse.Section<SearchResponse.QuestionItem> questionSection =
                new SearchResponse.Section<>(Collections.emptyList(), 0L);
        SearchResponse.Section<SearchResponse.UserItem> userSection =
                new SearchResponse.Section<>(Collections.emptyList(), 0L);

        boolean allMode = "all".equals(type);

        if (allMode || "articles".equals(type)) {
            Pageable pageable = buildArticlePageable(request, allMode);
            articleSection = buildArticleSection(keyword, request, pageable);
        }
        if (allMode || "questions".equals(type)) {
            Pageable pageable = buildQuestionPageable(request, allMode);
            questionSection = buildQuestionSection(keyword, request, pageable);
        }
        if (allMode || "users".equals(type)) {
            Pageable pageable = buildUserPageable(request, allMode);
            userSection = buildUserSection(keyword, pageable);
        }

        long total = (articleSection == null ? 0 : articleSection.getTotal())
                + (questionSection == null ? 0 : questionSection.getTotal())
                + (userSection == null ? 0 : userSection.getTotal());

        logSearch(keyword, type, total);

        return SearchResponse.builder()
                .articles(articleSection)
                .questions(questionSection)
                .users(userSection)
                .videos(new SearchResponse.Section<>(Collections.emptyList(), 0L))
                .total(total)
                .build();
    }

    @Override
    public SearchSuggestionResponse suggestions(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new SearchSuggestionResponse(Collections.emptyList());
        }
        List<String> items = searchLogRepository.findSuggestions(keyword.trim(), PageRequest.of(0, SUGGESTION_LIMIT));
        return new SearchSuggestionResponse(items);
    }

    @Override
    public SearchHotKeywordResponse hotKeywords() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = searchLogRepository.findHotKeywords(start, PageRequest.of(0, HOT_LIMIT));
        List<SearchHotKeywordResponse.HotKeyword> keywords = rows.stream()
                .map(row -> new SearchHotKeywordResponse.HotKeyword((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
        return new SearchHotKeywordResponse(keywords);
    }

    @Override
    public SearchHistoryResponse history() {
        Long userId = AuthContextHolder.getCurrentUser().map(u -> u.getId()).orElse(null);
        if (userId == null) {
            return new SearchHistoryResponse(Collections.emptyList());
        }
        List<String> items = searchLogRepository.findHistoryByUser(userId, PageRequest.of(0, HISTORY_LIMIT));
        return new SearchHistoryResponse(items);
    }

    @Override
    public void clearHistory() {
        Long userId = AuthContextHolder.getCurrentUser().map(u -> u.getId()).orElse(null);
        if (userId == null) {
            return;
        }
        searchLogRepository.deleteByUser_Id(userId);
    }

    private Pageable buildArticlePageable(SearchRequest request, boolean limited) {
        int page = limited ? 0 : Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1);
        int size = limited ? ALL_MODE_LIMIT : Optional.ofNullable(request.getPageSize()).orElse(20);
        Sort sort = switch (request.normalizedSort()) {
            case "latest" -> Sort.by(Sort.Order.desc("createdAt"));
            case "popular" -> Sort.by(Sort.Order.desc("viewCount"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
        return PageRequest.of(page, size, sort);
    }

    private Pageable buildQuestionPageable(SearchRequest request, boolean limited) {
        int page = limited ? 0 : Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1);
        int size = limited ? ALL_MODE_LIMIT : Optional.ofNullable(request.getPageSize()).orElse(20);
        Sort sort = switch (request.normalizedSort()) {
            case "latest" -> Sort.by(Sort.Order.desc("createdAt"));
            case "popular" -> Sort.by(Sort.Order.desc("viewCount"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
        return PageRequest.of(page, size, sort);
    }

    private Pageable buildUserPageable(SearchRequest request, boolean limited) {
        int page = limited ? 0 : Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1);
        int size = limited ? ALL_MODE_LIMIT : Optional.ofNullable(request.getPageSize()).orElse(20);
        return PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
    }

    private SearchResponse.Section<SearchResponse.ArticleItem> buildArticleSection(String keyword,
                                                                                  SearchRequest request,
                                                                                  Pageable pageable) {
        Specification<Article> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String likeExpression = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), likeExpression),
                    cb.like(cb.lower(root.get("summary")), likeExpression),
                    cb.like(cb.lower(root.get("content")), likeExpression)
            ));
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            if (request.getAuthorId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getAuthorId()));
            }
            if (request.getTagId() != null) {
                Join<Article, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(cb.equal(tagJoin.get("id"), request.getTagId()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Article> page = articleRepository.findAll(specification, pageable);
        List<SearchResponse.ArticleItem> items = page.stream()
                .map(article -> new SearchResponse.ArticleItem(
                        article.getId(),
                        article.getTitle(),
                        article.getSummary(),
                        article.getCoverImage(),
                        article.getViewCount(),
                        article.getLikeCount(),
                        article.getCommentCount(),
                        article.getCreatedAt(),
                        new SearchResponse.SimplifiedUser(
                                article.getUser().getId(),
                                article.getUser().getNickname(),
                                article.getUser().getAvatar()
                        )
                ))
                .collect(Collectors.toList());
        return new SearchResponse.Section<>(items, page.getTotalElements());
    }

    private SearchResponse.Section<SearchResponse.QuestionItem> buildQuestionSection(String keyword,
                                                                                    SearchRequest request,
                                                                                    Pageable pageable) {
        Specification<Question> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String likeExpression = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), likeExpression),
                    cb.like(cb.lower(root.get("description")), likeExpression)
            ));
            if (request.getAuthorId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getAuthorId()));
            }
            if (request.getTagId() != null) {
                Join<Question, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(cb.equal(tagJoin.get("id"), request.getTagId()));
                query.distinct(true);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Question> page = questionRepository.findAll(specification, pageable);
        List<SearchResponse.QuestionItem> items = page.stream()
                .map(question -> new SearchResponse.QuestionItem(
                        question.getId(),
                        question.getTitle(),
                        question.getDescription(),
                        question.getViewCount(),
                        question.getAnswerCount(),
                        question.getFollowCount(),
                        question.getCreatedAt(),
                        new SearchResponse.SimplifiedUser(
                                question.getUser().getId(),
                                question.getUser().getNickname(),
                                question.getUser().getAvatar()
                        )
                ))
                .collect(Collectors.toList());
        return new SearchResponse.Section<>(items, page.getTotalElements());
    }

    private SearchResponse.Section<SearchResponse.UserItem> buildUserSection(String keyword, Pageable pageable) {
        Specification<User> specification = (root, query, cb) -> {
            String likeExpression = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("username")), likeExpression),
                    cb.like(cb.lower(root.get("nickname")), likeExpression),
                    cb.like(cb.lower(root.get("bio")), likeExpression)
            );
        };
        Page<User> page = userRepository.findAll(specification, pageable);
        List<SearchResponse.UserItem> items = page.stream()
                .map(user -> new SearchResponse.UserItem(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        user.getAvatar(),
                        user.getBio()
                ))
                .collect(Collectors.toList());
        return new SearchResponse.Section<>(items, page.getTotalElements());
    }

    private void logSearch(String keyword, String type, long resultCount) {
        SearchLog.SearchLogBuilder builder = SearchLog.builder()
                .keyword(keyword)
                .searchType(type)
                .resultCount((int) Math.min(resultCount, Integer.MAX_VALUE));
        AuthContextHolder.getCurrentUser()
                .ifPresent(user -> builder.user(User.builder().id(user.getId()).build()));
        searchLogRepository.save(builder.build());
    }
}

