package com.example.back.service.impl;

import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.LikeRequest;
import com.example.back.dto.response.LikeStatusResponse;
import com.example.back.entity.Article;
import com.example.back.entity.Like;
import com.example.back.entity.User;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.ArticleRepository;
import com.example.back.repository.LikeRepository;
import com.example.back.repository.UserRepository;
import com.example.back.service.LikeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * 点赞服务实现
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private static final Set<String> SUPPORTED_TARGETS = Set.of("article", "answer", "video");

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public LikeStatusResponse like(LikeRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        String targetType = normalizeType(request.normalizedTargetType());
        Long targetId = Objects.requireNonNull(request.getTargetId(), "目标ID不能为空");
        TargetContext context = resolveTarget(targetType, targetId);

        boolean liked = likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, targetId).isPresent();
        if (!liked) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("用户不存在"));
            Like like = Like.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .user(user)
                    .build();
            likeRepository.save(like);
            context.increaseLikeCount();
            liked = true;
        }
        int count = Math.toIntExact(likeRepository.countByTargetTypeAndTargetId(targetType, targetId));
        context.syncLikeCount(count);
        return new LikeStatusResponse(count, liked);
    }

    @Override
    @Transactional
    public void cancel(String targetTypeParam, Long targetId) {
        Long userId = AuthContextHolder.requireUserId();
        String targetType = normalizeType(targetTypeParam == null ? null : targetTypeParam.trim().toLowerCase());
        Long finalTargetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        TargetContext context = resolveTarget(targetType, finalTargetId);
        likeRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, finalTargetId)
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    context.decreaseLikeCount();
                    int count = Math.toIntExact(likeRepository.countByTargetTypeAndTargetId(targetType, finalTargetId));
                    context.syncLikeCount(count);
                });
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new ValidationException("目标类型不能为空");
        }
        String normalized = type.trim().toLowerCase();
        if (!SUPPORTED_TARGETS.contains(normalized)) {
            throw new ValidationException("暂不支持该点赞类型");
        }
        return normalized;
    }

    private TargetContext resolveTarget(String type, Long targetId) {
        return switch (type) {
            case "article" -> {
                Article article = articleRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("文章不存在"));
                yield TargetContext.forArticle(article);
            }
            case "answer", "video" -> TargetContext.simple();
            default -> throw new ValidationException("暂不支持该点赞类型");
        };
    }

    private record TargetContext(Article article) {
        static TargetContext forArticle(Article article) {
            return new TargetContext(article);
        }

        static TargetContext simple() {
            return new TargetContext(null);
        }

        void increaseLikeCount() {
            if (article != null) {
                article.setLikeCount(article.getLikeCount() + 1);
            }
        }

        void decreaseLikeCount() {
            if (article != null) {
                article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
            }
        }

        void syncLikeCount(int count) {
            if (article != null) {
                article.setLikeCount(count);
            }
        }
    }
}

