package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.context.AuthUser;
import com.example.back.dto.request.CommentCreateRequest;
import com.example.back.dto.request.CommentQueryRequest;
import com.example.back.dto.request.CommentUpdateRequest;
import com.example.back.dto.response.CommentLikeResponse;
import com.example.back.dto.response.CommentListItemResponse;
import com.example.back.dto.response.CommentResponse;
import com.example.back.entity.*;
import com.example.back.exception.ForbiddenException;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.*;
import com.example.back.service.CommentService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Set<String> SUPPORTED_TARGETS = Set.of("article", "answer");

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final AnswerRepository answerRepository;
    private final LikeRepository likeRepository;

    @Override
    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        String targetType = normalizeTargetType(request.normalizedTargetType());
        TargetContext targetContext = resolveTarget(targetType, request.getTargetId());

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new NotFoundException("父评论不存在"));
            if (!Objects.equals(parent.getTargetType(), targetType) || !Objects.equals(parent.getTargetId(), request.getTargetId())) {
                throw new ValidationException("父评论与目标内容不匹配");
            }
        }

        Comment comment = Comment.builder()
                .targetType(targetType)
                .targetId(request.getTargetId())
                .user(user)
                .parent(parent)
                .content(request.getContent().trim())
                .build();
        Comment saved = commentRepository.save(comment);

        increaseTargetCommentCount(targetContext);
        if (parent != null) {
            parent.increaseReplyCount();
        }

        return toCommentResponse(saved);
    }

    @Override
    public PageResult<CommentListItemResponse> getComments(CommentQueryRequest request) {
        String targetType = normalizeTargetType(request.normalizedTargetType());
        if (request.getTargetId() == null) {
            throw new ValidationException("目标ID不能为空");
        }
        resolveTarget(targetType, request.getTargetId());

        Pageable pageable = buildPageable(request);
        Specification<Comment> specification = buildSpecification(targetType, request.getTargetId());
        Page<Comment> page = commentRepository.findAll(specification, pageable);

        List<Long> commentIds = page.stream().map(Comment::getId).toList();
        // 递归加载所有嵌套回复
        List<Comment> allReplies = loadAllReplies(commentIds);
        List<Long> allIds = new ArrayList<>(commentIds);
        allIds.addAll(allReplies.stream().map(Comment::getId).toList());
        Map<Long, Boolean> likedMap = loadLikedMap(allIds);
        Map<Long, List<Comment>> replyMap = allReplies.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .collect(Collectors.groupingBy(comment -> comment.getParent().getId()));

        List<CommentListItemResponse> items = page.stream()
                .map(comment -> toCommentListItem(comment, likedMap, replyMap))
                .collect(Collectors.toList());

        return PageResult.of(items, pageable.getPageNumber() + 1, pageable.getPageSize(), page.getTotalElements());
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentUpdateRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("评论不存在"));
        ensureOwner(comment, userId);
        comment.setContent(request.getContent().trim());
        return toCommentResponse(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Long userId = AuthContextHolder.requireUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("评论不存在"));
        ensureDeletePermission(comment, userId);
        if (comment.getDeletedAt() != null) {
            return;
        }
        comment.setDeletedAt(LocalDateTime.now());
        comment.setStatus(0);
        TargetContext target = resolveTarget(comment.getTargetType(), comment.getTargetId());
        decreaseTargetCommentCount(target);
        if (comment.getParent() != null) {
            comment.getParent().decreaseReplyCount();
        }
    }

    @Override
    @Transactional
    public CommentLikeResponse likeComment(Long commentId) {
        Long userId = AuthContextHolder.requireUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("评论不存在"));
        boolean liked = likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, "comment", commentId).isPresent();
        if (!liked) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("用户不存在"));
            Like like = Like.builder()
                    .targetType("comment")
                    .targetId(commentId)
                    .user(user)
                    .build();
            likeRepository.save(like);
            comment.increaseLikeCount();
            liked = true;
        }
        return new CommentLikeResponse(comment.getLikeCount(), liked);
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId) {
        Long userId = AuthContextHolder.requireUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("评论不存在"));
        likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, "comment", commentId)
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    comment.decreaseLikeCount();
                });
    }

    @Override
    @Transactional
    public void pinComment(Long commentId) {
        Long userId = AuthContextHolder.requireUserId();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("评论不存在"));
        TargetContext target = resolveTarget(comment.getTargetType(), comment.getTargetId());
        if (!Objects.equals(target.ownerId(), userId)) {
            throw new ForbiddenException("无权置顶该评论");
        }
        commentRepository.findByTargetTypeAndTargetIdAndPinned(comment.getTargetType(), comment.getTargetId(), 1)
                .forEach(existing -> existing.unpin());
        comment.pin();
    }

    private Map<Long, Boolean> loadLikedMap(List<Long> commentIds) {
        Long currentUserId = AuthContextHolder.getCurrentUser().map(AuthUser::getId).orElse(null);
        if (currentUserId == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return likeRepository.findByUser_IdAndTargetTypeAndTargetIdIn(currentUserId, "comment", commentIds).stream()
                .collect(Collectors.toMap(Like::getTargetId, like -> true));
    }

    /**
     * 递归加载所有嵌套回复（最多支持5层嵌套）
     */
    private List<Comment> loadAllReplies(List<Long> parentIds) {
        if (parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Comment> allReplies = new ArrayList<>();
        List<Long> currentLevelIds = new ArrayList<>(parentIds);
        int maxDepth = 5; // 限制最大嵌套深度
        
        for (int depth = 0; depth < maxDepth && !currentLevelIds.isEmpty(); depth++) {
            List<Comment> currentLevelReplies = commentRepository.findByParentIdIn(currentLevelIds);
            if (currentLevelReplies.isEmpty()) {
                break;
            }
            allReplies.addAll(currentLevelReplies);
            // 准备下一层的 parentIds
            currentLevelIds = currentLevelReplies.stream()
                    .map(Comment::getId)
                    .toList();
        }
        
        return allReplies;
    }

    private CommentListItemResponse toCommentListItem(Comment comment,
                                                      Map<Long, Boolean> likedMap,
                                                      Map<Long, List<Comment>> replyMap) {
        List<CommentListItemResponse> replies = replyMap.getOrDefault(comment.getId(), Collections.emptyList()).stream()
                .map(reply -> toCommentListItem(reply, likedMap, replyMap))
                .collect(Collectors.toList());
        return CommentListItemResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .parentId(comment.getParent() == null ? null : comment.getParent().getId())
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .liked(likedMap.getOrDefault(comment.getId(), false))
                .pinned(comment.isPinned())
                .createdAt(comment.getCreatedAt())
                .author(new CommentListItemResponse.AuthorInfo(
                        comment.getUser().getId(),
                        comment.getUser().getNickname(),
                        comment.getUser().getAvatar()
                ))
                .replies(replies)
                .build();
    }

    private Pageable buildPageable(CommentQueryRequest request) {
        int page = Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1);
        int size = Optional.ofNullable(request.getPageSize()).orElse(20);
        Sort sort;
        switch (request.normalizedSort()) {
            case "oldest" -> sort = Sort.by(Sort.Order.asc("createdAt"));
            case "hot" -> sort = Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("replyCount"), Sort.Order.desc("createdAt"));
            default -> sort = Sort.by(Sort.Order.desc("createdAt"));
        }
        return PageRequest.of(page, size, sort);
    }

    private Specification<Comment> buildSpecification(String targetType, Long targetId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("targetType"), targetType));
            predicates.add(cb.equal(root.get("targetId"), targetId));
            predicates.add(cb.isNull(root.get("parent")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void ensureOwner(Comment comment, Long userId) {
        if (!Objects.equals(comment.getUser().getId(), userId)) {
            throw new ForbiddenException("无权操作该评论");
        }
    }

    private void ensureDeletePermission(Comment comment, Long userId) {
        if (Objects.equals(comment.getUser().getId(), userId)) {
            return;
        }
        TargetContext target = resolveTarget(comment.getTargetType(), comment.getTargetId());
        if (!Objects.equals(target.ownerId(), userId)) {
            throw new ForbiddenException("无权删除该评论");
        }
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private String normalizeTargetType(String targetType) {
        if (targetType == null) {
            throw new ValidationException("目标类型不能为空");
        }
        String normalized = targetType.trim().toLowerCase();
        if (!SUPPORTED_TARGETS.contains(normalized)) {
            throw new ValidationException("暂不支持该目标类型");
        }
        return normalized;
    }

    private TargetContext resolveTarget(String targetType, Long targetId) {
        if (targetId == null) {
            throw new ValidationException("目标ID不能为空");
        }
        return switch (targetType) {
            case "article" -> {
                Article article = articleRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("文章不存在"));
                yield TargetContext.article(article);
            }
            case "answer" -> {
                Answer answer = answerRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("回答不存在"));
                yield TargetContext.answer(answer);
            }
            default -> throw new ValidationException("暂不支持该目标类型");
        };
    }

    private void increaseTargetCommentCount(TargetContext ctx) {
        if (ctx.article() != null) {
            ctx.article().setCommentCount(ctx.article().getCommentCount() + 1);
        } else if (ctx.answer() != null) {
            ctx.answer().setCommentCount(ctx.answer().getCommentCount() + 1);
        }
    }

    private void decreaseTargetCommentCount(TargetContext ctx) {
        if (ctx.article() != null) {
            ctx.article().setCommentCount(Math.max(0, ctx.article().getCommentCount() - 1));
        } else if (ctx.answer() != null) {
            ctx.answer().setCommentCount(Math.max(0, ctx.answer().getCommentCount() - 1));
        }
    }

    private record TargetContext(String targetType, Article article, Answer answer, Long ownerId) {
        static TargetContext article(Article article) {
            return new TargetContext("article", article, null, article.getUser().getId());
        }

        static TargetContext answer(Answer answer) {
            return new TargetContext("answer", null, answer, answer.getUser().getId());
        }
    }
}

