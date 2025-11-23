package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.AnswerQueryRequest;
import com.example.back.dto.request.AnswerRequest;
import com.example.back.dto.request.AnswerVoteRequest;
import com.example.back.dto.response.AnswerDetailResponse;
import com.example.back.dto.response.AnswerResponse;
import com.example.back.dto.response.AnswerVoteResponse;
import com.example.back.entity.*;
import com.example.back.enums.AnswerVoteType;
import com.example.back.exception.ForbiddenException;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.*;
import com.example.back.service.AnswerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 回答服务实现
 */
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerVoteRepository answerVoteRepository;

    @Override
    @Transactional
    public AnswerResponse createAnswer(Long questionId, AnswerRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("问题不存在"));
        Answer answer = Answer.builder()
                .question(question)
                .user(user)
                .content(request.getContent())
                .build();
        Answer saved = answerRepository.save(answer);
        question.increaseAnswerCount();
        return toAnswerResponse(saved);
    }

    @Override
    @Transactional
    public AnswerResponse updateAnswer(Long answerId, AnswerRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Answer answer = loadAnswer(answerId);
        ensureOwner(answer, userId);
        answer.setContent(request.getContent());
        return toAnswerResponse(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long answerId) {
        Long userId = AuthContextHolder.requireUserId();
        Answer answer = loadAnswer(answerId);
        ensureOwner(answer, userId);
        Question question = answer.getQuestion();
        answer.setDeletedAt(LocalDateTime.now());
        question.decreaseAnswerCount();
        if (question.getBestAnswer() != null && question.getBestAnswer().getId().equals(answer.getId())) {
            question.setBestAnswer(null);
        }
    }

    @Override
    public PageResult<AnswerDetailResponse> getAnswers(Long questionId, AnswerQueryRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("问题不存在"));
        Pageable pageable = buildPageable(request);
        Specification<Answer> specification = (root, query, cb) -> cb.and(
                cb.equal(root.get("question"), question),
                cb.isNull(root.get("deletedAt"))
        );
        Page<Answer> page = answerRepository.findAll(specification, pageable);
        Long currentUserId = AuthContextHolder.getCurrentUser()
                .map(auth -> auth.getId())
                .orElse(null);
        Map<Long, AnswerVoteType> voteMap = Collections.emptyMap();
        if (currentUserId != null && page.hasContent()) {
            List<Long> answerIds = page.stream().map(Answer::getId).collect(Collectors.toList());
            voteMap = answerVoteRepository.findByAnswer_IdInAndUser_Id(answerIds, currentUserId).stream()
                    .collect(Collectors.toMap(v -> v.getAnswer().getId(), AnswerVote::getVoteType));
        }
        Map<Long, AnswerVoteType> finalVoteMap = voteMap;
        List<AnswerDetailResponse> items = page.stream()
                .map(answer -> toAnswerDetailResponse(answer, finalVoteMap.get(answer.getId())))
                .collect(Collectors.toList());
        return PageResult.of(items, pageable.getPageNumber() + 1, pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    @Transactional
    public AnswerVoteResponse voteAnswer(Long answerId, AnswerVoteRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        Answer answer = loadAnswer(answerId);
        AnswerVoteType type = parseVoteType(request.getType());

        AnswerVote vote = answerVoteRepository.findByAnswerAndUser(answer, user).orElse(null);
        if (vote == null) {
            vote = AnswerVote.builder()
                    .answer(answer)
                    .user(user)
                    .voteType(type)
                    .build();
            adjustVoteCounters(answer, null, type);
            answerVoteRepository.save(vote);
        } else if (vote.getVoteType() != type) {
            adjustVoteCounters(answer, vote.getVoteType(), type);
            vote.setVoteType(type);
        }

        return new AnswerVoteResponse(
                answer.getUpvoteCount(),
                answer.getDownvoteCount(),
                type == AnswerVoteType.UPVOTE,
                type == AnswerVoteType.DOWNVOTE
        );
    }

    @Override
    @Transactional
    public void cancelVote(Long answerId) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        Answer answer = loadAnswer(answerId);
        answerVoteRepository.findByAnswerAndUser(answer, user).ifPresent(vote -> {
            adjustVoteCounters(answer, vote.getVoteType(), null);
            answerVoteRepository.delete(vote);
        });
    }

    private Answer loadAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("回答不存在"));
        if (answer.getDeletedAt() != null) {
            throw new NotFoundException("回答不存在");
        }
        return answer;
    }

    private void ensureOwner(Answer answer, Long userId) {
        if (!answer.getUser().getId().equals(userId)) {
            throw new ForbiddenException("无权操作该回答");
        }
    }

    private Pageable buildPageable(AnswerQueryRequest request) {
        int page = Optional.ofNullable(request.getPage()).orElse(1) - 1;
        int size = Optional.ofNullable(request.getPageSize()).orElse(20);
        String sort = request.normalizedSort();
        Sort sortOption;
        switch (sort) {
            case "latest" -> sortOption = Sort.by(Sort.Order.desc("createdAt"));
            case "oldest" -> sortOption = Sort.by(Sort.Order.asc("createdAt"));
            case "hot" -> sortOption = Sort.by(Sort.Order.desc("commentCount"), Sort.Order.desc("upvoteCount"));
            default -> sortOption = Sort.by(Sort.Order.desc("upvoteCount"));
        }
        return PageRequest.of(Math.max(page, 0), size, sortOption);
    }

    private AnswerVoteType parseVoteType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new ValidationException("投票类型不能为空");
        }
        String normalized = type.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "upvote" -> AnswerVoteType.UPVOTE;
            case "downvote" -> AnswerVoteType.DOWNVOTE;
            default -> throw new ValidationException("未知的投票类型");
        };
    }

    private void adjustVoteCounters(Answer answer, AnswerVoteType oldType, AnswerVoteType newType) {
        if (oldType == newType) {
            return;
        }
        if (oldType == AnswerVoteType.UPVOTE) {
            answer.setUpvoteCount(Math.max(0, answer.getUpvoteCount() - 1));
        } else if (oldType == AnswerVoteType.DOWNVOTE) {
            answer.setDownvoteCount(Math.max(0, answer.getDownvoteCount() - 1));
        }

        if (newType == AnswerVoteType.UPVOTE) {
            answer.setUpvoteCount(answer.getUpvoteCount() + 1);
        } else if (newType == AnswerVoteType.DOWNVOTE) {
            answer.setDownvoteCount(answer.getDownvoteCount() + 1);
        }
    }

    private AnswerResponse toAnswerResponse(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .build();
    }

    private AnswerDetailResponse toAnswerDetailResponse(Answer answer, AnswerVoteType voteType) {
        boolean upvoted = voteType == AnswerVoteType.UPVOTE;
        boolean downvoted = voteType == AnswerVoteType.DOWNVOTE;
        return AnswerDetailResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .upvoteCount(answer.getUpvoteCount())
                .downvoteCount(answer.getDownvoteCount())
                .commentCount(answer.getCommentCount())
                .best(answer.isBestAnswer())
                .upvoted(upvoted)
                .downvoted(downvoted)
                .createdAt(answer.getCreatedAt())
                .author(new AnswerDetailResponse.AuthorInfo(
                        answer.getUser().getId(),
                        answer.getUser().getNickname(),
                        answer.getUser().getAvatar()
                ))
                .build();
    }
}

