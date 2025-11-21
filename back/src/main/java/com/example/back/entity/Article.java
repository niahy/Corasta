package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 文章实体
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "articles")
@SQLRestriction("deleted_at IS NULL")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 200)
    private String summary;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(length = 100, unique = true)
    private String slug;

    @Column(nullable = false)
    private Integer status;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Builder.Default
    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    @Builder.Default
    @Column(name = "share_count", nullable = false)
    private Integer shareCount = 0;

    @Builder.Default
    @Column(name = "is_top", nullable = false)
    private Integer isTop = 0;

    @Builder.Default
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 1;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "article_tags",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
}

