package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.QuestionQueryRequest;
import com.example.back.dto.request.QuestionRequest;
import com.example.back.dto.response.QuestionDetailResponse;
import com.example.back.dto.response.QuestionListItemResponse;
import com.example.back.dto.response.QuestionResponse;
import com.example.back.entity.*;
import com.example.back.exception.ForbiddenException;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.*;
import com.example.back.service.QuestionService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问题服务实现
 */
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionFollowRepository questionFollowRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        Question question = Question.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .relatedArticle(fetchRelatedArticle(request.getRelatedArticleId()))
                .build();
        question.setTags(resolveTags(request.getTags()));
        Question saved = questionRepository.save(question);
        return toQuestionResponse(saved);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Question question = loadQuestion(questionId);
        ensureOwner(question, userId);
        question.setTitle(request.getTitle());
        question.setDescription(request.getDescription());
        question.setRelatedArticle(fetchRelatedArticle(request.getRelatedArticleId()));

        Set<Tag> tags = resolveTags(request.getTags());
        question.getTags().clear();
        question.getTags().addAll(tags);

        return toQuestionResponse(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId) {
        Long userId = AuthContextHolder.requireUserId();
        Question question = loadQuestion(questionId);
        ensureOwner(question, userId);
        question.setDeletedAt(LocalDateTime.now());
    }

    @Override
    @Transactional
    public QuestionDetailResponse getQuestionDetail(Long questionId) {
        Question question = loadQuestion(questionId);
        question.increaseViewCount();

        Long currentUserId = AuthContextHolder.getCurrentUser()
                .map(authUser -> authUser.getId())
                .orElse(null);
        boolean following = false;
        if (currentUserId != null) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new NotFoundException("用户不存在"));
            following = questionFollowRepository.existsByQuestionAndUser(question, user);
        }

        return toQuestionDetailResponse(question, following);
    }

    @Override
    public PageResult<QuestionListItemResponse> getQuestionList(QuestionQueryRequest request) {
        Pageable pageable = buildPageable(request);
        Specification<Question> specification = buildSpecification(request);
        Page<Question> page = questionRepository.findAll(specification, pageable);
        List<QuestionListItemResponse> items = page.stream()
                .map(this::toQuestionListItemResponse)
                .collect(Collectors.toList());
        return PageResult.of(items, pageable.getPageNumber() + 1, pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    @Transactional
    public void followQuestion(Long questionId) {
        Long userId = AuthContextHolder.requireUserId();
        Question question = loadQuestion(questionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        if (questionFollowRepository.existsByQuestionAndUser(question, user)) {
            return;
        }
        QuestionFollow follow = QuestionFollow.builder()
                .question(question)
                .user(user)
                .build();
        questionFollowRepository.save(follow);
        question.increaseFollowCount();
    }

    @Override
    @Transactional
    public void unfollowQuestion(Long questionId) {
        Long userId = AuthContextHolder.requireUserId();
        Question question = loadQuestion(questionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        questionFollowRepository.findByQuestionAndUser(question, user).ifPresent(follow -> {
            questionFollowRepository.delete(follow);
            question.decreaseFollowCount();
        });
    }

    @Override
    @Transactional
    public void markBestAnswer(Long questionId, Long answerId) {
        Long userId = AuthContextHolder.requireUserId();
        Question question = loadQuestion(questionId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("回答不存在"));
        if (!answer.getQuestion().getId().equals(questionId)) {
            throw new ValidationException("回答不属于该问题");
        }
        boolean allowed = question.getUser().getId().equals(userId);
        if (!allowed && question.getRelatedArticle() != null && question.getRelatedArticle().getUser() != null) {
            allowed = question.getRelatedArticle().getUser().getId().equals(userId);
        }
        if (!allowed) {
            throw new ForbiddenException("无权标记最佳回答");
        }
        if (question.getBestAnswer() != null && question.getBestAnswer().getId().equals(answerId)) {
            return;
        }
        if (question.getBestAnswer() != null) {
            question.getBestAnswer().unmarkBest();
        }
        answer.markBest();
        question.setBestAnswer(answer);
    }

    private Question loadQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("问题不存在"));
    }

    private void ensureOwner(Question question, Long userId) {
        if (!question.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权操作该问题");
        }
    }

    private Article fetchRelatedArticle(Long articleId) {
        if (articleId == null) {
            return null;
        }
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ValidationException("关联文章不存在"));
    }

    private Set<Tag> resolveTags(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return new HashSet<>();
        }
        List<String> normalized = names.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(name -> name.length() > 50 ? name.substring(0, 50) : name)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            return new HashSet<>();
        }
        Map<String, Tag> existing = tagRepository.findByNameIn(normalized).stream()
                .collect(Collectors.toMap(Tag::getName, tag -> tag));
        Set<Tag> tags = new LinkedHashSet<>();
        for (String name : normalized) {
            tags.add(existing.getOrDefault(name, Tag.builder().name(name).build()));
        }
        return tags;
    }

    private Pageable buildPageable(QuestionQueryRequest request) {
        int page = Optional.ofNullable(request.getPage()).orElse(1) - 1;
        int size = Optional.ofNullable(request.getPageSize()).orElse(20);
        String sort = request.normalizedSort();
        Sort sortOption;
        switch (sort) {
            case "popular" -> sortOption = Sort.by(Sort.Order.desc("followCount"), Sort.Order.desc("viewCount"));
            case "answers" -> sortOption = Sort.by(Sort.Order.desc("answerCount"));
            default -> sortOption = Sort.by(Sort.Order.desc("createdAt"));
        }
        return PageRequest.of(Math.max(page, 0), size, sortOption);
    }

    private Specification<Question> buildSpecification(QuestionQueryRequest request) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            }

            String keyword = request.normalizedKeyword();
            if (keyword != null) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("description"), like)
                ));
            }

            if (request.getTagId() != null) {
                Join<Question, Tag> tagJoin = root.join("tags", JoinType.LEFT);
                predicates.add(cb.equal(tagJoin.get("id"), request.getTagId()));
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private QuestionResponse toQuestionResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .createdAt(question.getCreatedAt())
                .build();
    }

    private QuestionDetailResponse toQuestionDetailResponse(Question question, boolean following) {
        return QuestionDetailResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .description(question.getDescription())
                .viewCount(question.getViewCount())
                .answerCount(question.getAnswerCount())
                .followCount(question.getFollowCount())
                .bestAnswerId(question.getBestAnswer() == null ? null : question.getBestAnswer().getId())
                .following(following)
                .createdAt(question.getCreatedAt())
                .author(new QuestionDetailResponse.AuthorInfo(
                        question.getUser().getId(),
                        question.getUser().getNickname(),
                        question.getUser().getAvatar()
                ))
                .relatedArticle(question.getRelatedArticle() == null ? null :
                        new QuestionDetailResponse.RelatedArticleInfo(
                                question.getRelatedArticle().getId(),
                                question.getRelatedArticle().getTitle()
                        ))
                .tags(question.getTags().stream()
                        .map(tag -> new QuestionDetailResponse.TagInfo(tag.getId(), tag.getName()))
                        .collect(Collectors.toList()))
                .build();
    }

    private QuestionListItemResponse toQuestionListItemResponse(Question question) {
        return QuestionListItemResponse.builder()
                .id(question.getId())
                .title(question.getTitle())
                .description(question.getDescription())
                .answerCount(question.getAnswerCount())
                .followCount(question.getFollowCount())
                .createdAt(question.getCreatedAt())
                .author(new QuestionListItemResponse.AuthorInfo(
                        question.getUser().getId(),
                        question.getUser().getNickname(),
                        question.getUser().getAvatar()
                ))
                .tags(question.getTags().stream()
                        .map(tag -> new QuestionListItemResponse.TagInfo(tag.getId(), tag.getName()))
                        .collect(Collectors.toList()))
                .build();
    }
}

