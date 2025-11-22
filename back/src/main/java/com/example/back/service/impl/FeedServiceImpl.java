package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.FeedQueryRequest;
import com.example.back.dto.response.FeedItemResponse;
import com.example.back.entity.Article;
import com.example.back.entity.Question;
import com.example.back.repository.ArticleRepository;
import com.example.back.repository.FollowRepository;
import com.example.back.repository.QuestionRepository;
import com.example.back.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Feed服务实现
 */
@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private static final int MAX_FETCH_MULTIPLIER = 3;

    private final FollowRepository followRepository;
    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;

    @Override
    public PageResult<FeedItemResponse> getFeed(FeedQueryRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(userId);
        int page = Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1);
        int size = Optional.ofNullable(request.getPageSize()).orElse(20);
        if (followingIds.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page + 1, size, 0L);
        }
        return switch (request.normalizedType()) {
            case "articles" -> buildArticleFeed(followingIds, page, size);
            case "questions" -> buildQuestionFeed(followingIds, page, size);
            case "videos" -> PageResult.of(Collections.emptyList(), page + 1, size, 0L);
            default -> buildMixedFeed(followingIds, page, size);
        };
    }

    private PageResult<FeedItemResponse> buildArticleFeed(List<Long> userIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // 使用 @EntityGraph 预加载 user 关联
        Page<Article> articlePage = articleRepository.findByUser_IdIn(userIds, pageable);
        List<FeedItemResponse> items = articlePage.stream()
                .map(this::toArticleFeedItem)
                .collect(Collectors.toList());
        return PageResult.of(items, page + 1, size, articlePage.getTotalElements());
    }

    private PageResult<FeedItemResponse> buildQuestionFeed(List<Long> userIds, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // 使用 @EntityGraph 预加载 user 关联
        Page<Question> questionPage = questionRepository.findByUser_IdIn(userIds, pageable);
        List<FeedItemResponse> items = questionPage.stream()
                .map(this::toQuestionFeedItem)
                .collect(Collectors.toList());
        return PageResult.of(items, page + 1, size, questionPage.getTotalElements());
    }

    private PageResult<FeedItemResponse> buildMixedFeed(List<Long> userIds, int page, int size) {
        int fetchSize = Math.min(500, (page + 1) * size * MAX_FETCH_MULTIPLIER);
        Pageable fetchPageable = PageRequest.of(0, fetchSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 使用 @EntityGraph 预加载 user 关联
        List<FeedItemResponse> articleItems = articleRepository.findByUser_IdIn(userIds, fetchPageable).stream()
                .map(this::toArticleFeedItem)
                .collect(Collectors.toList());
        List<FeedItemResponse> questionItems = questionRepository.findByUser_IdIn(userIds, fetchPageable).stream()
                .map(this::toQuestionFeedItem)
                .collect(Collectors.toList());

        List<FeedItemResponse> merged = Stream.concat(articleItems.stream(), questionItems.stream())
                .sorted(Comparator.comparing(FeedItemResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int fromIndex = Math.min(page * size, merged.size());
        int toIndex = Math.min(fromIndex + size, merged.size());
        List<FeedItemResponse> pageItems = merged.subList(fromIndex, toIndex);

        long total = articleRepository.countByUser_IdIn(userIds) + questionRepository.countByUser_IdIn(userIds);
        return PageResult.of(pageItems, page + 1, size, total);
    }

    private FeedItemResponse toArticleFeedItem(Article article) {
        return FeedItemResponse.builder()
                .type("article")
                .content(new FeedItemResponse.Content(
                        article.getId(),
                        article.getTitle(),
                        article.getSummary(),
                        article.getCoverImage(),
                        article.getViewCount(),
                        article.getLikeCount(),
                        article.getCommentCount(),
                        null
                ))
                .author(new FeedItemResponse.AuthorInfo(
                        article.getUser().getId(),
                        article.getUser().getNickname(),
                        article.getUser().getAvatar()
                ))
                .createdAt(article.getCreatedAt())
                .build();
    }

    private FeedItemResponse toQuestionFeedItem(Question question) {
        return FeedItemResponse.builder()
                .type("question")
                .content(new FeedItemResponse.Content(
                        question.getId(),
                        question.getTitle(),
                        question.getDescription(),
                        null,
                        question.getViewCount(),
                        null,
                        question.getFollowCount(),
                        question.getAnswerCount()
                ))
                .author(new FeedItemResponse.AuthorInfo(
                        question.getUser().getId(),
                        question.getUser().getNickname(),
                        question.getUser().getAvatar()
                ))
                .createdAt(question.getCreatedAt())
                .build();
    }
}

