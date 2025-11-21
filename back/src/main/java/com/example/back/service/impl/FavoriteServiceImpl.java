package com.example.back.service.impl;

import com.example.back.common.PageResult;
import com.example.back.context.AuthContextHolder;
import com.example.back.dto.request.FavoriteFolderRequest;
import com.example.back.dto.request.FavoriteQueryRequest;
import com.example.back.dto.request.FavoriteRequest;
import com.example.back.dto.response.FavoriteFolderResponse;
import com.example.back.dto.response.FavoriteItemResponse;
import com.example.back.entity.*;
import com.example.back.exception.ForbiddenException;
import com.example.back.exception.NotFoundException;
import com.example.back.exception.ValidationException;
import com.example.back.repository.*;
import com.example.back.service.FavoriteService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private static final Set<String> SUPPORTED_TARGETS = Set.of("article", "answer");

    private final FavoriteFolderRepository favoriteFolderRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final AnswerRepository answerRepository;

    @Override
    @Transactional
    public FavoriteFolderResponse createFolder(FavoriteFolderRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        FavoriteFolder folder = FavoriteFolder.builder()
                .user(user)
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        FavoriteFolder saved = favoriteFolderRepository.save(folder);
        return toFolderResponse(saved, 0);
    }

    @Override
    public List<FavoriteFolderResponse> listFolders() {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        List<FavoriteFolder> folders = favoriteFolderRepository.findByUserOrderBySortOrderAscIdAsc(user);
        Map<Long, Integer> counts = folders.stream()
                .collect(Collectors.toMap(FavoriteFolder::getId,
                        folder -> Math.toIntExact(favoriteRepository.countByFolder(folder))));
        return folders.stream()
                .map(folder -> toFolderResponse(folder, counts.getOrDefault(folder.getId(), 0)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFolder(Long folderId) {
        if (folderId == null) {
            throw new ValidationException("收藏夹ID不能为空");
        }
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        FavoriteFolder folder = favoriteFolderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("收藏夹不存在"));
        if (!Objects.equals(folder.getUser().getId(), user.getId())) {
            throw new ForbiddenException("无权删除该收藏夹");
        }
        folder.setDeletedAt(java.time.LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateFolderOrder(Long folderId, Integer sortOrder) {
        if (folderId == null) {
            throw new ValidationException("收藏夹ID不能为空");
        }
        Long userId = AuthContextHolder.requireUserId();
        FavoriteFolder folder = favoriteFolderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("收藏夹不存在"));
        if (!Objects.equals(folder.getUser().getId(), userId)) {
            throw new ForbiddenException("无权操作该收藏夹");
        }
        folder.setSortOrder(Optional.ofNullable(sortOrder).orElse(0));
    }

    @Override
    @Transactional
    public void favorite(FavoriteRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
        String targetType = normalizeType(request.normalizedTargetType());
        Long targetId = Objects.requireNonNull(request.getTargetId(), "目标ID不能为空");
        TargetContext context = resolveTarget(targetType, targetId);

        FavoriteFolder folder = null;
        if (request.getFolderId() != null) {
            folder = favoriteFolderRepository.findById(request.getFolderId())
                    .orElseThrow(() -> new NotFoundException("收藏夹不存在"));
            if (!Objects.equals(folder.getUser().getId(), userId)) {
                throw new ForbiddenException("无权使用该收藏夹");
            }
        }

        Favorite favorite = favoriteRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .orElse(null);
        if (favorite == null) {
            favorite = Favorite.builder()
                    .user(user)
                    .targetType(targetType)
                    .targetId(targetId)
                    .folder(folder)
                    .build();
            favoriteRepository.save(favorite);
            context.increaseFavoriteCount();
        } else {
            favorite.setFolder(folder);
        }
    }

    @Override
    @Transactional
    public void cancel(String targetTypeParam, Long targetId) {
        Long userId = AuthContextHolder.requireUserId();
        String targetType = normalizeType(targetTypeParam == null ? null : targetTypeParam.trim().toLowerCase());
        Long finalTargetId = Objects.requireNonNull(targetId, "目标ID不能为空");
        TargetContext context = resolveTarget(targetType, finalTargetId);
        favoriteRepository.findByUser_IdAndTargetTypeAndTargetId(userId, targetType, finalTargetId)
                .ifPresent(favorite -> {
                    favoriteRepository.delete(favorite);
                    context.decreaseFavoriteCount();
                });
    }

    @Override
    public PageResult<FavoriteItemResponse> listFavorites(FavoriteQueryRequest request) {
        Long userId = AuthContextHolder.requireUserId();
        Pageable pageable = PageRequest.of(
                Math.max(0, Optional.ofNullable(request.getPage()).orElse(1) - 1),
                Optional.ofNullable(request.getPageSize()).orElse(20),
                Sort.by(Sort.Order.desc("createdAt"))
        );
        Specification<Favorite> specification = buildSpecification(userId, request);
        Page<Favorite> page = favoriteRepository.findAll(specification, pageable);

        Map<Long, Article> articleMap = loadArticles(page.getContent());
        Map<Long, Answer> answerMap = loadAnswers(page.getContent());

        List<FavoriteItemResponse> items = page.stream()
                .map(favorite -> toFavoriteItemResponse(favorite, articleMap, answerMap))
                .collect(Collectors.toList());

        return PageResult.of(items, pageable.getPageNumber() + 1, pageable.getPageSize(), page.getTotalElements());
    }

    private Specification<Favorite> buildSpecification(Long userId, FavoriteQueryRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (request.getFolderId() != null) {
                predicates.add(cb.equal(root.get("folder").get("id"), request.getFolderId()));
            }

            if (request.getTargetType() != null) {
                predicates.add(cb.equal(root.get("targetType"), normalizeType(request.normalizedTargetType())));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private FavoriteFolderResponse toFolderResponse(FavoriteFolder folder, Integer itemCount) {
        return FavoriteFolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .itemCount(itemCount)
                .createdAt(folder.getCreatedAt())
                .build();
    }

    private FavoriteItemResponse toFavoriteItemResponse(Favorite favorite,
                                                        Map<Long, Article> articleMap,
                                                        Map<Long, Answer> answerMap) {
        FavoriteItemResponse.TargetInfo targetInfo = buildTargetInfo(favorite, articleMap, answerMap);
        FavoriteItemResponse.FolderInfo folderInfo = favorite.getFolder() == null ? null :
                new FavoriteItemResponse.FolderInfo(favorite.getFolder().getId(), favorite.getFolder().getName());
        return FavoriteItemResponse.builder()
                .id(favorite.getId())
                .targetType(favorite.getTargetType())
                .target(targetInfo)
                .folder(folderInfo)
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    private FavoriteItemResponse.TargetInfo buildTargetInfo(Favorite favorite,
                                                            Map<Long, Article> articleMap,
                                                            Map<Long, Answer> answerMap) {
        switch (favorite.getTargetType()) {
            case "article" -> {
                Article article = articleMap.get(favorite.getTargetId());
                if (article == null) {
                    return new FavoriteItemResponse.TargetInfo(favorite.getTargetId(), "已删除的文章", "-", null);
                }
                return new FavoriteItemResponse.TargetInfo(
                        article.getId(),
                        article.getTitle(),
                        article.getUser().getNickname(),
                        article.getCreatedAt()
                );
            }
            case "answer" -> {
                Answer answer = answerMap.get(favorite.getTargetId());
                if (answer == null) {
                    return new FavoriteItemResponse.TargetInfo(favorite.getTargetId(), "已删除的回答", "-", null);
                }
                String title = answer.getQuestion() == null ? "回答详情" : "回答：" + answer.getQuestion().getTitle();
                return new FavoriteItemResponse.TargetInfo(
                        answer.getId(),
                        title,
                        answer.getUser().getNickname(),
                        answer.getCreatedAt()
                );
            }
            default -> throw new ValidationException("暂不支持该收藏类型");
        }
    }

    private Map<Long, Article> loadArticles(List<Favorite> favorites) {
        List<Long> ids = favorites.stream()
                .filter(fav -> "article".equals(fav.getTargetType()))
                .map(Favorite::getTargetId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return articleRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Article::getId, article -> article));
    }

    private Map<Long, Answer> loadAnswers(List<Favorite> favorites) {
        List<Long> ids = favorites.stream()
                .filter(fav -> "answer".equals(fav.getTargetType()))
                .map(Favorite::getTargetId)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return answerRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Answer::getId, answer -> answer));
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new ValidationException("目标类型不能为空");
        }
        String normalized = type.trim().toLowerCase();
        if (!SUPPORTED_TARGETS.contains(normalized)) {
            throw new ValidationException("暂不支持该收藏类型");
        }
        return normalized;
    }

    private TargetContext resolveTarget(String type, Long targetId) {
        return switch (type) {
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
            default -> throw new ValidationException("暂不支持该收藏类型");
        };
    }

    private record TargetContext(Article article, Answer answer) {
        static TargetContext article(Article article) {
            return new TargetContext(article, null);
        }

        static TargetContext answer(Answer answer) {
            return new TargetContext(null, answer);
        }

        void increaseFavoriteCount() {
            if (article != null) {
                article.setFavoriteCount(article.getFavoriteCount() + 1);
            }
        }

        void decreaseFavoriteCount() {
            if (article != null) {
                article.setFavoriteCount(Math.max(0, article.getFavoriteCount() - 1));
            }
        }
    }
}

