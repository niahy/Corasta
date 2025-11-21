package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 评论实体
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
@SQLRestriction("deleted_at IS NULL")
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    @Builder.Default
    @Column(name = "is_pinned", nullable = false)
    private Integer pinned = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isPinned() {
        return Integer.valueOf(1).equals(this.pinned);
    }

    public void pin() {
        this.pinned = 1;
    }

    public void unpin() {
        this.pinned = 0;
    }

    public void increaseLikeCount() {
        this.likeCount = this.likeCount + 1;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void increaseReplyCount() {
        this.replyCount = this.replyCount + 1;
    }

    public void decreaseReplyCount() {
        this.replyCount = Math.max(0, this.replyCount - 1);
    }

    public boolean isTopLevel() {
        return this.parent == null;
    }
}

