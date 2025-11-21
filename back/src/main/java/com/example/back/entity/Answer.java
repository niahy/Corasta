package com.example.back.entity;

import com.example.back.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 回答实体
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "answers")
@SQLRestriction("deleted_at IS NULL")
public class Answer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "upvote_count", nullable = false)
    private Integer upvoteCount = 0;

    @Builder.Default
    @Column(name = "downvote_count", nullable = false)
    private Integer downvoteCount = 0;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Builder.Default
    @Column(name = "is_best", nullable = false)
    private Integer isBest = 0;

    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Builder.Default
    @Column(name = "audit_status", nullable = false)
    private Integer auditStatus = 1;

    @Column(name = "audit_remark", length = 500)
    private String auditRemark;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isBestAnswer() {
        return Integer.valueOf(1).equals(this.isBest);
    }

    public void markBest() {
        this.isBest = 1;
    }

    public void unmarkBest() {
        this.isBest = 0;
    }
}

