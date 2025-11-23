package com.example.back.service.impl;

import com.example.back.common.Constants;
import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.DashboardContentQueryRequest;
import com.example.back.dto.request.InteractionHistoryRequest;
import com.example.back.dto.response.*;
import com.example.back.dto.response.DashboardContentResponse;
import com.example.back.entity.*;
import com.example.back.repository.*;
import com.example.back.service.DashboardService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户内容后台服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_LIMIT = 6;
    private static final int TODO_LIMIT = 3;
    private static final int TREND_DAYS = 7;

    private final ArticleRepository articleRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;

    @Override
    public DashboardOverviewResponse getOverview() {
        Long userId = AuthContextHolder.requireUserId();

        long articleCount = articleRepository.countByUser_Id(userId);
        long draftCount = articleRepository.countByUser_IdAndStatus(userId, Constants.STATUS_DRAFT);
        long questionCount = questionRepository.countByUser_Id(userId);
        long answerCount = answerRepository.countByUser_Id(userId);
        long followerCount = followRepository.countByFollowing_Id(userId);

        long totalViews = articleRepository.sumViewCountByUser(userId)
                + questionRepository.sumViewCountByUser(userId);
        long totalLikes = articleRepository.sumLikeCountByUser(userId)
                + answerRepository.sumUpvoteCountByUser(userId);
        long totalFavorites = articleRepository.sumFavoriteCountByUser(userId);
        long totalComments = articleRepository.sumCommentCountByUser(userId)
                + answerRepository.sumCommentCountByUser(userId);

        List<DashboardOverviewResponse.ContentCard> recentContents = buildRecentContents(userId);
        List<DashboardOverviewResponse.TodoItem> todoItems = buildTodoItems(userId);

        DashboardOverviewResponse.Stats stats = DashboardOverviewResponse.Stats.builder()
                .articleCount(articleCount)
                .draftCount(draftCount)
                .questionCount(questionCount)
                .answerCount(answerCount)
                .followerCount(followerCount)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalFavorites(totalFavorites)
                .totalComments(totalComments)
                .build();

        return DashboardOverviewResponse.builder()
                .stats(stats)
                .recentContents(recentContents)
                .pendingItems(todoItems)
                .build();
    }

    @Override
    public DashboardContentResponse getContent() {
        Long userId = AuthContextHolder.requireUserId();

        long articleCount = articleRepository.countByUser_Id(userId);
        long questionCount = questionRepository.countByUser_Id(userId);
        long answerCount = answerRepository.countByUser_Id(userId);

        long totalViews = articleRepository.sumViewCountByUser(userId)
                + questionRepository.sumViewCountByUser(userId);
        long totalLikes = articleRepository.sumLikeCountByUser(userId)
                + answerRepository.sumUpvoteCountByUser(userId);
        long totalComments = articleRepository.sumCommentCountByUser(userId)
                + answerRepository.sumCommentCountByUser(userId);

        DashboardContentResponse.Statistics statistics = DashboardContentResponse.Statistics.builder()
                .articleCount(articleCount)
                .questionCount(questionCount)
                .answerCount(answerCount)
                .totalViewCount(totalViews)
                .totalLikeCount(totalLikes)
                .totalCommentCount(totalComments)
                .build();

        List<DashboardContentResponse.RecentItem> recentArticles = articleRepository
                .findTop5ByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(article -> DashboardContentResponse.RecentItem.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .viewCount(article.getViewCount())
                        .likeCount(article.getLikeCount())
                        .commentCount(article.getCommentCount())
                        .createdAt(article.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<DashboardContentResponse.RecentItem> recentQuestions = questionRepository
                .findTop5ByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(question -> DashboardContentResponse.RecentItem.builder()
                        .id(question.getId())
                        .title(question.getTitle())
                        .viewCount(question.getViewCount())
                        .likeCount(question.getAnswerCount())
                        .commentCount(question.getAnswerCount())
                        .createdAt(question.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<DashboardContentResponse.RecentItem> recentAnswers = answerRepository
                .findTop5ByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(answer -> DashboardContentResponse.RecentItem.builder()
                        .id(answer.getId())
                        .title(Optional.ofNullable(answer.getQuestion())
                                .map(Question::getTitle)
                                .orElse("回答"))
                        .viewCount(answer.getUpvoteCount())
                        .likeCount(answer.getUpvoteCount())
                        .commentCount(answer.getCommentCount())
                        .createdAt(answer.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardContentResponse.builder()
                .statistics(statistics)
                .recentArticles(recentArticles)
                .recentQuestions(recentQuestions)
                .recentAnswers(recentAnswers)
                .build();
    }

    @Override
    public PageResult<DashboardContentItemResponse> listContents(DashboardContentQueryRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Pageable pageable = buildPageable(request.getPage(), request.getPageSize());
        String type = request.normalizedType();

        return switch (type) {
            case "articles" -> mapArticlePage(
                    articleRepository.findAll(buildArticleSpec(userId, request), pageable));
            case "questions" -> mapQuestionPage(
                    questionRepository.findAll(buildQuestionSpec(userId, request), pageable));
            case "answers" -> mapAnswerPage(
                    answerRepository.findAll(buildAnswerSpec(userId, request), pageable));
            default -> PageResult.of(Collections.emptyList(), request.getPage(), request.getPageSize(), 0L);
        };
    }

    @Override
    public PageResult<InteractionHistoryItemResponse> interactionHistory(InteractionHistoryRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Pageable pageable = buildPageable(request.getPage(), request.getPageSize());
        String type = request.normalizedType();

        return switch (type) {
            case "likes" -> mapLikeHistory(userId, pageable, request.getPage(), request.getPageSize());
            case "favorites" -> mapFavoriteHistory(userId, pageable, request.getPage(), request.getPageSize());
            case "comments" -> mapCommentHistory(userId, request.getPage(), request.getPageSize());
            case "answers" -> mapAnswerHistory(userId, request.getPage(), request.getPageSize());
            default -> PageResult.of(Collections.emptyList(), request.getPage(), request.getPageSize(), 0L);
        };
    }

    @Override
    public DashboardTrendResponse getTrends() {
        Long userId = AuthContextHolder.requireUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(TREND_DAYS - 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        Map<LocalDate, Long> articleTrend = toDateMap(
                articleRepository.countDailyArticles(userId, start, end));
        Map<LocalDate, Long> questionTrend = toDateMap(
                questionRepository.countDailyQuestions(userId, start, end));
        Map<LocalDate, Long> answerTrend = toDateMap(
                answerRepository.countDailyAnswers(userId, start, end));
        Map<LocalDate, Long> followerTrend = toDateMap(
                followRepository.countDailyFollowers(userId, start, end));

        List<DashboardTrendResponse.TrendPoint> contentTrend = iterateDays(startDate, endDate).stream()
                .map(date -> DashboardTrendResponse.TrendPoint.builder()
                        .date(date)
                        .value(articleTrend.getOrDefault(date, 0L)
                                + questionTrend.getOrDefault(date, 0L)
                                + answerTrend.getOrDefault(date, 0L))
                        .build())
                .collect(Collectors.toList());

        List<DashboardTrendResponse.TrendPoint> followerTrendPoints = iterateDays(startDate, endDate).stream()
                .map(date -> DashboardTrendResponse.TrendPoint.builder()
                        .date(date)
                        .value(followerTrend.getOrDefault(date, 0L))
                        .build())
                .collect(Collectors.toList());

        return DashboardTrendResponse.builder()
                .contentTrend(contentTrend)
                .followerTrend(followerTrendPoints)
                .build();
    }

    private List<DashboardOverviewResponse.ContentCard> buildRecentContents(Long userId) {
        List<DashboardOverviewResponse.ContentCard> items = new ArrayList<>();

        articleRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId)
                .forEach(article -> items.add(DashboardOverviewResponse.ContentCard.builder()
                        .id(article.getId())
                        .type("article")
                        .title(article.getTitle())
                        .status(resolveArticleStatus(article.getStatus()))
                        .viewCount(article.getViewCount())
                        .likeCount(article.getLikeCount())
                        .commentCount(article.getCommentCount())
                        .createdAt(article.getCreatedAt())
                        .build()));

        questionRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId)
                .forEach(question -> items.add(DashboardOverviewResponse.ContentCard.builder()
                        .id(question.getId())
                        .type("question")
                        .title(question.getTitle())
                        .status("published")
                        .viewCount(question.getViewCount())
                        .likeCount(question.getAnswerCount())
                        .commentCount(question.getAnswerCount())
                        .createdAt(question.getCreatedAt())
                        .build()));

        answerRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId)
                .forEach(answer -> items.add(DashboardOverviewResponse.ContentCard.builder()
                        .id(answer.getId())
                        .type("answer")
                        .title(Optional.ofNullable(answer.getQuestion())
                                .map(Question::getTitle)
                                .orElse("回答"))
                        .status(answer.getIsBest() == 1 ? "best" : "published")
                        .viewCount(answer.getUpvoteCount())
                        .likeCount(answer.getUpvoteCount())
                        .commentCount(answer.getCommentCount())
                        .createdAt(answer.getCreatedAt())
                        .build()));

        return items.stream()
                .sorted(Comparator.comparing(DashboardOverviewResponse.ContentCard::getCreatedAt).reversed())
                .limit(RECENT_LIMIT)
                .collect(Collectors.toList());
    }

    private List<DashboardOverviewResponse.TodoItem> buildTodoItems(Long userId) {
        return questionRepository.findTop5ByUser_IdAndAnswerCountOrderByCreatedAtAsc(userId, 0).stream()
                .limit(TODO_LIMIT)
                .map(question -> DashboardOverviewResponse.TodoItem.builder()
                        .type("question")
                        .description("问题《" + question.getTitle() + "》还没有回答")
                        .targetId(question.getId())
                        .createdAt(question.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private Specification<Article> buildArticleSpec(Long userId, DashboardContentQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            String status = request.normalizedStatus();
            if ("draft".equals(status)) {
                predicates.add(cb.equal(root.get("status"), Constants.STATUS_DRAFT));
            } else if ("published".equals(status)) {
                predicates.add(cb.equal(root.get("status"), Constants.STATUS_PUBLISHED));
            } else if ("private".equals(status)) {
                predicates.add(cb.equal(root.get("status"), Constants.STATUS_PRIVATE));
            }
            if (StringUtils.hasText(request.getKeyword())) {
                String like = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), like));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Question> buildQuestionSpec(Long userId, DashboardContentQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if ("answered".equals(request.normalizedStatus())) {
                predicates.add(cb.greaterThan(root.get("answerCount"), 0));
            } else if ("pending".equals(request.normalizedStatus())) {
                predicates.add(cb.equal(root.get("answerCount"), 0));
            }
            if (StringUtils.hasText(request.getKeyword())) {
                String like = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), like));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Answer> buildAnswerSpec(Long userId, DashboardContentQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if ("best".equals(request.normalizedStatus())) {
                predicates.add(cb.equal(root.get("isBest"), 1));
            }
            if (StringUtils.hasText(request.getKeyword())) {
                String like = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("content")), like));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PageResult<DashboardContentItemResponse> mapArticlePage(Page<Article> page) {
        List<DashboardContentItemResponse> items = page.stream()
                .map(article -> DashboardContentItemResponse.builder()
                        .id(article.getId())
                        .type("article")
                        .title(article.getTitle())
                        .status(resolveArticleStatus(article.getStatus()))
                        .viewCount(article.getViewCount())
                        .likeCount(article.getLikeCount())
                        .commentCount(article.getCommentCount())
                        .createdAt(article.getCreatedAt())
                        .updatedAt(article.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(items, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private PageResult<DashboardContentItemResponse> mapQuestionPage(Page<Question> page) {
        List<DashboardContentItemResponse> items = page.stream()
                .map(question -> DashboardContentItemResponse.builder()
                        .id(question.getId())
                        .type("question")
                        .title(question.getTitle())
                        .status(question.getAnswerCount() > 0 ? "answered" : "pending")
                        .viewCount(question.getViewCount())
                        .likeCount(question.getAnswerCount())
                        .commentCount(question.getAnswerCount())
                        .createdAt(question.getCreatedAt())
                        .updatedAt(question.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(items, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private PageResult<DashboardContentItemResponse> mapAnswerPage(Page<Answer> page) {
        List<DashboardContentItemResponse> items = page.stream()
                .map(answer -> DashboardContentItemResponse.builder()
                        .id(answer.getId())
                        .type("answer")
                        .title(Optional.ofNullable(answer.getQuestion())
                                .map(Question::getTitle)
                                .orElse("回答"))
                        .status(answer.getIsBest() == 1 ? "best" : "published")
                        .viewCount(answer.getUpvoteCount())
                        .likeCount(answer.getUpvoteCount())
                        .commentCount(answer.getCommentCount())
                        .createdAt(answer.getCreatedAt())
                        .updatedAt(answer.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(items, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    private PageResult<InteractionHistoryItemResponse> mapLikeHistory(Long userId, Pageable pageable, int page, int pageSize) {
        Page<Like> likePage = likeRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        TargetCache cache = buildTargetCache(likePage.getContent());
        List<InteractionHistoryItemResponse> items = likePage.stream()
                .map(like -> buildHistoryItem("like", like.getCreatedAt(), like.getTargetType(), like.getTargetId(), cache))
                .collect(Collectors.toList());
        return PageResult.of(items, page, pageSize, likePage.getTotalElements());
    }

    private PageResult<InteractionHistoryItemResponse> mapFavoriteHistory(Long userId, Pageable pageable, int page, int pageSize) {
        Page<Favorite> favoritePage = favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        TargetCache cache = buildTargetCache(favoritePage.getContent());
        List<InteractionHistoryItemResponse> items = favoritePage.stream()
                .map(favorite -> buildHistoryItem("favorite", favorite.getCreatedAt(), favorite.getTargetType(), favorite.getTargetId(), cache))
                .collect(Collectors.toList());
        return PageResult.of(items, page, pageSize, favoritePage.getTotalElements());
    }

    private PageResult<InteractionHistoryItemResponse> mapCommentHistory(Long userId, int page, int pageSize) {
        Page<Comment> commentPage = commentRepository.findByUser_Id(userId,
                PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        TargetCache cache = buildTargetCache(commentPage.getContent());
        List<InteractionHistoryItemResponse> items = commentPage.stream()
                .map(comment -> {
                    InteractionHistoryItemResponse item = buildHistoryItem("comment", comment.getCreatedAt(),
                            comment.getTargetType(), comment.getTargetId(), cache);
                    item.setTargetExcerpt(comment.getContent());
                    return item;
                })
                .collect(Collectors.toList());
        return PageResult.of(items, page, pageSize, commentPage.getTotalElements());
    }

    private PageResult<InteractionHistoryItemResponse> mapAnswerHistory(Long userId, int page, int pageSize) {
        Page<Answer> answerPage = answerRepository.findByUser_Id(userId,
                PageRequest.of(Math.max(0, page - 1), pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<InteractionHistoryItemResponse> items = answerPage.stream()
                .map(answer -> InteractionHistoryItemResponse.builder()
                        .type("answer")
                        .actionTime(answer.getCreatedAt())
                        .targetType("question")
                        .targetId(Optional.ofNullable(answer.getQuestion()).map(Question::getId).orElse(null))
                        .targetTitle(Optional.ofNullable(answer.getQuestion()).map(Question::getTitle).orElse("回答"))
                        .targetExcerpt(trimContent(answer.getContent()))
                        .targetOwnerId(Optional.ofNullable(answer.getQuestion()).map(q -> q.getUser().getId()).orElse(null))
                        .targetOwnerName(Optional.ofNullable(answer.getQuestion()).map(q -> q.getUser().getNickname()).orElse(null))
                        .likeCount(answer.getUpvoteCount())
                        .commentCount(answer.getCommentCount())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(items, page, pageSize, answerPage.getTotalElements());
    }

    private InteractionHistoryItemResponse buildHistoryItem(String type, LocalDateTime actionTime,
                                                            String targetType, Long targetId, TargetCache cache) {
        TargetSummary summary = cache.lookup(targetType, targetId);
        InteractionHistoryItemResponse response = InteractionHistoryItemResponse.builder()
                .type(type)
                .actionTime(actionTime)
                .targetType(targetType)
                .targetId(targetId)
                .targetTitle(summary != null ? summary.title() : "内容已删除")
                .targetExcerpt(summary != null ? summary.excerpt() : null)
                .targetOwnerId(summary != null ? summary.ownerId() : null)
                .targetOwnerName(summary != null ? summary.ownerName() : null)
                .likeCount(summary != null ? summary.likeCount() : null)
                .commentCount(summary != null ? summary.commentCount() : null)
                .build();
        return response;
    }

    private TargetCache buildTargetCache(List<?> records) {
        if (CollectionUtils.isEmpty(records)) {
            return TargetCache.empty();
        }
        Map<String, Set<Long>> targetMap = new HashMap<>();
        for (Object record : records) {
            if (record instanceof Like like) {
                targetMap.computeIfAbsent(like.getTargetType(), k -> new HashSet<>()).add(like.getTargetId());
            } else if (record instanceof Favorite favorite) {
                targetMap.computeIfAbsent(favorite.getTargetType(), k -> new HashSet<>()).add(favorite.getTargetId());
            } else if (record instanceof Comment comment) {
                targetMap.computeIfAbsent(comment.getTargetType(), k -> new HashSet<>()).add(comment.getTargetId());
            }
        }
        Map<String, Map<Long, TargetSummary>> cache = new HashMap<>();
        targetMap.forEach((type, ids) -> {
            if ("article".equals(type)) {
                cache.put(type, articleRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Article::getId, article -> new TargetSummary(
                                article.getId(),
                                article.getTitle(),
                                article.getSummary(),
                                article.getUser().getId(),
                                article.getUser().getNickname(),
                                article.getLikeCount(),
                                article.getCommentCount()
                        ))));
            } else if ("question".equals(type)) {
                cache.put(type, questionRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Question::getId, question -> new TargetSummary(
                                question.getId(),
                                question.getTitle(),
                                trimContent(question.getDescription()),
                                question.getUser().getId(),
                                question.getUser().getNickname(),
                                question.getAnswerCount(),
                                question.getAnswerCount()
                        ))));
            } else if ("answer".equals(type)) {
                cache.put(type, answerRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Answer::getId, answer -> new TargetSummary(
                                answer.getId(),
                                Optional.ofNullable(answer.getQuestion()).map(Question::getTitle).orElse("回答"),
                                trimContent(answer.getContent()),
                                answer.getUser().getId(),
                                answer.getUser().getNickname(),
                                answer.getUpvoteCount(),
                                answer.getCommentCount()
                        ))));
            } else if ("comment".equals(type)) {
                cache.put(type, commentRepository.findAllById(ids).stream()
                        .collect(Collectors.toMap(Comment::getId, comment -> new TargetSummary(
                                comment.getId(),
                                "评论",
                                trimContent(comment.getContent()),
                                comment.getUser().getId(),
                                comment.getUser().getNickname(),
                                comment.getLikeCount(),
                                comment.getReplyCount()
                        ))));
            }
        });
        return new TargetCache(cache);
    }

    private Pageable buildPageable(Integer page, Integer pageSize) {
        int pageIndex = Math.max(0, Optional.ofNullable(page).orElse(1) - 1);
        int size = Math.min(Constants.MAX_PAGE_SIZE, Optional.ofNullable(pageSize).orElse(Constants.DEFAULT_PAGE_SIZE));
        return PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String resolveArticleStatus(Integer status) {
        if (Objects.equals(status, Constants.STATUS_DRAFT)) {
            return "draft";
        }
        if (Objects.equals(status, Constants.STATUS_PRIVATE)) {
            return "private";
        }
        return "published";
    }

    private Map<LocalDate, Long> toDateMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = row[0] instanceof LocalDate localDate
                    ? localDate
                    : LocalDate.parse(row[0].toString());
            Long value = row[1] instanceof Number number ? number.longValue() : Long.parseLong(row[1].toString());
            map.put(date, value);
        }
        return map;
    }

    private List<LocalDate> iterateDays(LocalDate start, LocalDate end) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate pointer = start;
        while (!pointer.isAfter(end)) {
            days.add(pointer);
            pointer = pointer.plusDays(1);
        }
        return days;
    }

    private String trimContent(String content) {
        if (!StringUtils.hasText(content)) {
            return null;
        }
        return content.length() > 120 ? content.substring(0, 120) + "..." : content;
    }

    private record TargetSummary(Long id, String title, String excerpt,
                                 Long ownerId, String ownerName, Integer likeCount, Integer commentCount) {
    }

    private static final class TargetCache {
        private final Map<String, Map<Long, TargetSummary>> delegate;

        private TargetCache(Map<String, Map<Long, TargetSummary>> delegate) {
            this.delegate = delegate;
        }

        static TargetCache empty() {
            return new TargetCache(Collections.emptyMap());
        }

        TargetSummary lookup(String type, Long id) {
            return delegate.getOrDefault(type, Collections.emptyMap()).get(id);
        }
    }
}

